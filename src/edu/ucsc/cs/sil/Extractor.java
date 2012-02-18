package edu.ucsc.cs.sil;
import java.io.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;

import org.evolizer.changedistiller.model.classifiers.ChangeType;

import edu.ucsc.cs.sil.bow.BagOfWordsPlus;

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
		ResultSet commitRS = stmt.executeQuery("select s.id as commit_id, author_id, author_date, rev, length(message) as log_length "
				+ "from scmlog s"+
						" where s.repository_id in("+repoID+") order by author_date");
		int cumulative_change_count = 0;
		int cumulative_bug_count = 0;
		int new_rev_loc = 0;
		while (commitRS.next()) {
			cumulative_change_count++;
			int commitID = commitRS.getInt("commit_id");
			Commit commit = new Commit(commitID);
			BagOfWordsPlus contentBOW = new BagOfWordsPlus("new_source");
			BagOfWordsPlus aDeltaBOW = new BagOfWordsPlus("added_delta"), dDeltaBOW = new BagOfWordsPlus("deleted_delta");
			
			Statement stmt1 = conn.createStatement();
			ResultSet file = stmt1
					.executeQuery("select * " +
							"from action_files af join files f on af.file_id=f.id " +
							"where commit_id="+commit.getID() +
							" and f.file_name like '%.java'");
			while (file.next()) {
				// Generate ASF diff data
				char action_type = file.getString("action_type").charAt(0);
				int fileID = file.getInt("file_id");
				switch (action_type) {
				case 'C': commit.incrFilesCopied(); break;
				case 'M': commit.processModify(fileID); break;
				case 'D': commit.processDelete(fileID); break;
				case 'A': commit.processAdd(fileID); break;
				case 'V': commit.processRename(fileID);break;
				}
				
				// Generate BOW+ features
				String content = commit.getNewContent(fileID);
				if(content != null){
					contentBOW.add(content);
					new_rev_loc += content.split("\n").length;
				}
				
				Statement stmt2 = conn.createStatement();
				ResultSet rs = stmt2.executeQuery("select patch from patches " +
						"where commit_id="+commit.getID()+" and file_id="+fileID);
				if(rs.next()){
					String patch = rs.getString(1);
					String addedDelta = extractDelta(patch, '+');
					String deletedDelta = extractDelta(patch, '-');
					
					aDeltaBOW.add(addedDelta);
					dDeltaBOW.add(deletedDelta);
				}else{
					if(action_type != 'V'){
						Logger logger = MyLogger.getLogger();
						logger.warning("Patch for file "+fileID+" at commit "+
								commit.getID()+" not found, actioin type: "+action_type);					
					}
				}
				stmt2.close();
			}

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
			features.put(getOrCreateIndex("log_length"), commitRS.getInt("log_length"));
			features.put(getOrCreateIndex("changed_LOC"), changedLOC);
			features.put(getOrCreateIndex("new_rev_loc"), new_rev_loc);
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
			for(String term : termFreq.keySet()){
				Integer freq = termFreq.get(term);
				if(cumulative_change_count <= Integer.valueOf(args[0])) {
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
