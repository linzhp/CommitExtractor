package edu.ucsc.cs.sil;
import java.io.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;

import org.evolizer.changedistiller.model.classifiers.ChangeType;

import edu.ucsc.cs.sil.bow.BagOfWords;
import edu.ucsc.cs.sil.bow.BagOfWordsPlus;
import edu.ucsc.cs.sil.bow.BagOfWordsPlusPlus;

/**
 * Depends on CVSAnalY extensions:
 * 'BugFixMessage','Patches','Hunks','HunkBlame','FileTypes','Content', 'CommitsLOC'
 *
 */

public class Extractor {

	private static Map<String, Integer> attrIndex;

	/**
	 * @param args args[0] is the number of revisions to generate feature vocabulary
	 */
	public static void main(String[] args) throws Exception{
		Connection conn = DatabaseManager.getConnection();
		Statement stmt;
		
		Properties prop = new Properties();
		prop.load(new FileInputStream("config.properties"));
		String repoID=prop.getProperty("RepositoryID");
		
		String dataFile = prop.getProperty("DataFile");
		if(dataFile == null)
			dataFile = "output.libsvm";
		FileWriter fWriter = new FileWriter(dataFile, false);
		
		// Fetching commit data
		stmt = conn.createStatement();
		ResultSet commitRS = stmt.executeQuery("select s.id as commit_id, author_id, author_date, rev, message "
				+ "from scmlog s"+
						" where s.repository_id in("+repoID+") order by author_date");
		int cumulative_change_count = 0;
		int cumulative_bug_count = 0;
		int newRevLOC = 0;
		while (commitRS.next()) {
			cumulative_change_count++;
			int commitID = commitRS.getInt("commit_id");
			Commit commit = new Commit(commitID);
			BagOfWordsPlus contentBOW = new BagOfWordsPlus("new_source");
			BagOfWordsPlus aDeltaBOW = new BagOfWordsPlus("added_delta"), dDeltaBOW = new BagOfWordsPlus("deleted_delta");
			BagOfWordsPlusPlus fileBOW = new BagOfWordsPlusPlus("file_path");
			
			Statement stmt1 = conn.createStatement();
			ResultSet file = stmt1
					.executeQuery("select * " +
							"from actions " +
							"where commit_id="+commit.getID() +
							" and current_file_path like '%.java'");
			while (file.next()) {
				extractASTDelta(commit, file);
				// Generate BOW+ features
				newRevLOC += extractSrcFeatures(commit,
						contentBOW, aDeltaBOW, dDeltaBOW, file);
				// Generate file path features
				fileBOW.add(file.getString("current_file_path"));
			}
			
			// Extract log message BOW
			BagOfWords logBOW = new BagOfWords("log");
			String log = commitRS.getString("message");
			logBOW.add(log);

			// Getting commit meta data
			ResultSet lines = stmt1.executeQuery("select added, removed" +
					" from commits_lines where commit_id="+commit.getID());
			lines.next();
			int changedLOC = lines.getInt("added");
			changedLOC += lines.getInt("removed");
			stmt1.close();
			// Constructing commit instance
			StringBuilder line = new StringBuilder();
			Set<String> fixRevs = getFixeRevs(commit.getID());
			if(fixRevs.size()>0){
				line.append(1);
				cumulative_bug_count++;
			}
			else
				line.append(0);
			Map<Integer, Integer> features = new TreeMap<Integer, Integer>();
			features.put(getOrCreateIndex("files_copied"), commit.getFilesCopied());
			features.put(getOrCreateIndex("log_length"), log.length());
			features.put(getOrCreateIndex("changed_LOC"), changedLOC);
			features.put(getOrCreateIndex("new_rev_loc"), newRevLOC);
			Timestamp date = commitRS.getTimestamp("author_date");
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			int hour = cal.get(Calendar.HOUR_OF_DAY);
			int day = cal.get(Calendar.DAY_OF_WEEK);
			features.put(getOrCreateIndex("commit_hour#"+hour), 1);
			features.put(getOrCreateIndex("commit_day#"+day), 1);
			features.put(getOrCreateIndex("author#"+commitRS.getInt("author_id")), 1);
			features.put(getOrCreateIndex("cumulative_change_count"), cumulative_change_count);
			features.put(getOrCreateIndex("cumulative_bug_count"), cumulative_bug_count);
			
			Map<String, Integer> termFreq = contentBOW.getTermFreq();
			termFreq.putAll(aDeltaBOW.getTermFreq());
			termFreq.putAll(dDeltaBOW.getTermFreq());
			termFreq.putAll(logBOW.getTermFreq());
			termFreq.putAll(fileBOW.getTermFreq());
			
			for(String term : termFreq.keySet()){
				Integer freq = termFreq.get(term);
				if(cumulative_change_count <= Integer.valueOf(prop.getProperty("VocabularyRev"))) {
					features.put(getOrCreateIndex(term), freq);
				} else{
					Integer index = getIndex(term);
					if(index != null)
						features.put(index, freq);
				}
			}
			
			for(ChangeType ct:ChangeType.values()){
				String category = ct.toString();
				Integer count = commit.categorizedChanges.get(category);
				if(count != null)
					features.put(getOrCreateIndex(category), count);
			}
			// Sort the indices
			SortedSet<Integer> indices = new TreeSet<Integer>(features.keySet());
			
			for(Integer i : indices){
				line.append(' ');
				line.append(i);
				line.append(':');
				line.append(features.get(i));
			}
			
			// Add comment
			line.append(" # ");
			for(String rev : fixRevs){
				line.append("<fix>");
				line.append(rev);
				line.append("</fix>");
			}
			
			line.append("<rev>");
			line.append(commitRS.getString("rev"));
			line.append("</rev>");
			
			line.append("<author_date>");
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HHmm");
			line.append(formatter.format(date));
			line.append("</author_date>");
			
			line.append('\n');
			fWriter.write(line.toString());
		}
		fWriter.close();
		//Log attribute indices
		String idxFile = prop.getProperty("IndexMappingFile");
		if(idxFile == null)
			idxFile = "attr.idx";
		fWriter = new FileWriter(idxFile);
		fWriter.write(attrIndex.toString());
		fWriter.close();
		conn.close();

	}

	private static int extractSrcFeatures(Commit commit, BagOfWordsPlus contentBOW, BagOfWordsPlus aDeltaBOW,
			BagOfWordsPlus dDeltaBOW, ResultSet file) throws SQLException,
			Exception {
		int fileID = file.getInt("file_id");
		String content = commit.getNewContent(fileID);
		int newRevLOC = 0;
		if(content != null){
			contentBOW.add(content);
			newRevLOC += content.split("\n").length;
		}
		
		Connection conn = DatabaseManager.getConnection();
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery("select patch from patches " +
				"where commit_id="+commit.getID()+" and file_id="+fileID);
		if(rs.next()){
			String patch = rs.getString(1);
			String addedDelta = extractDelta(patch, '+');
			String deletedDelta = extractDelta(patch, '-');
			
			aDeltaBOW.add(addedDelta);
			dDeltaBOW.add(deletedDelta);
		}else{
			char actionType = file.getString("type").charAt(0);
			if(actionType != 'V'){
				Logger logger = MyLogger.getLogger();
				logger.warning("Patch for file "+fileID+" at commit "+
						commit.getID()+" not found, action type: "+actionType);					
			}
		}
		stmt.close();
		return newRevLOC;
	}

	private static void extractASTDelta(Commit commit, ResultSet file)
			throws SQLException, Exception {
		int fileID = file.getInt("file_id");
		char actionType = file.getString("type").charAt(0);
		switch (actionType) {
		case 'C': commit.incrFilesCopied(); break;
		case 'M': commit.processModify(fileID); break;
		case 'D': commit.processDelete(fileID); break;
		case 'A': commit.processAdd(fileID); break;
		case 'V': commit.processRename(fileID);break;
		}
	}
	
	public static Set<String> getFixeRevs(int bug_commit_id) throws SQLException{
		Set<String> revs = new TreeSet<String>();
		Connection conn = DatabaseManager.getConnection();
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery("select rev, is_bug_fix " +
				"from scmlog c join hunks h on c.id=h.commit_id join hunk_blames hb on h.id=hb.hunk_id " +
				"where bug_commit_id="+bug_commit_id);
		while(rs.next()){
			if(rs.getBoolean("is_bug_fix")){
				revs.add(rs.getString("rev"));
			}
		}
		stmt.close();
		return revs;
	}
	
	public static String extractDelta(String patch, char sign) {
		String[] lines = patch.split("\n");
		StringBuilder delta = new StringBuilder();
		String pattern = "^\\"+sign+"[^\\"+sign+"]+.*";
		for(String l : lines){
			if(l.matches(pattern)){
				delta.append(l.subSequence(1, l.length()));
				delta.append('\n');
			}
		}
		return delta.toString();
	}
	
	public static int getOrCreateIndex(String attributeName){
		if(attrIndex == null){
			attrIndex = new TreeMap<String, Integer>();
		}
		Integer index = getIndex(attributeName);
		if(index == null){
			index = attrIndex.size()+1;
			attrIndex.put(attributeName, index);
		}
		return index;
	}
	
	public static Integer getIndex(String attrName) {
		return attrIndex.get(attrName);
	}
}
