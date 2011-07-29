import java.io.*;
import java.sql.*;
import java.util.*;

import org.evolizer.changedistiller.model.classifiers.ChangeType;

public class Extractor {

	private static Map<String, Integer> attrIndex;

	/**
	 * @param args
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws SQLException
	 */
	public static void main(String[] args) throws Exception{
		Connection conn = DatabaseManager.getConnection();
		Statement stmt = conn.createStatement();

		
		Properties prop = new Properties();
		prop.load(new FileInputStream("config.properties"));
		String repoID=prop.getProperty("RepositoryID");
		
		// Getting all bug commits
		Set<Integer> bugCommits = new TreeSet<Integer>();
		ResultSet bugCommitRS = stmt.executeQuery("select distinct bug_commit_id from" +
				" hunk_blames "+
				"where bug_commit_id in " +
				"(select id from scmlog where repository_id="+repoID+")");
		while(bugCommitRS.next()){
			bugCommits.add(bugCommitRS.getInt("bug_commit_id"));
		}
		
		FileWriter fWriter = new FileWriter("output.libsvm", false);
		
		// Fetching commit data
		ResultSet commitRS = stmt.executeQuery("select s.id as commit_id, length(message) as log_length "
				+ "from scmlog s"+
						" where s.repository_id="+repoID);
		while (commitRS.next()) {
			Commit commit = new Commit(commitRS.getInt("commit_id"));
			// Generating ASF diff data and content term frequency
			BagOfWords contentBOW = new BagOfWords("new_source");
			Statement stmt1 = conn.createStatement();
			ResultSet file = stmt1
					.executeQuery("select * " +
							"from action_files af join files f on af.file_id=f.id " +
							"where commit_id="+commit.getID() +
							" and f.file_name like '%.java'");
			while (file.next()) {
				char action_type = file.getString("action_type").charAt(0);
				int fileID = file.getInt("file_id");
				switch (action_type) {
				case 'C': commit.incrFilesCopied(); break;
				case 'M': commit.processModify(fileID); break;
				case 'D': commit.processDelete(fileID); break;
				case 'A': commit.processAdd(fileID); break;
				case 'V': commit.processRename(fileID);break;
				}
				String content = commit.getNewContent(fileID);
				if(content != null)
					contentBOW.add(content);
			}
			//Getting source code delta
			ResultSet rs = stmt1.executeQuery("select patch from patches where commit_id="+commit.getID());
			rs.next();
			String patch = rs.getString(1);
			String addedDelta = extractDelta(patch, '+');
			String deletedDelta = extractDelta(patch, '-');
			
			BagOfWords aDeltaBOW = new BagOfWords("added_delta"), dDeltaBOW = new BagOfWords("deleted_delta");
			aDeltaBOW.add(addedDelta);
			dDeltaBOW.add(deletedDelta);
			// Getting commit meta data
			ResultSet hunk = stmt1.executeQuery("select old_start_line, old_end_line, new_start_line, new_end_line" +
					" from hunks where commit_id="+commit.getID());
			int changedLOC=0;
			while(hunk.next()){
				int oldStartLine = hunk.getInt("old_start_line");
				int oldEndLine = hunk.getInt("old_end_line");
				if(oldEndLine>oldStartLine){
					changedLOC += oldEndLine-oldStartLine;
				}
				
				int newStartLine = hunk.getInt("new_start_line");
				int newEndLine = hunk.getInt("new_end_line");
				if(newEndLine > newStartLine){
					changedLOC += newEndLine - newStartLine;
				}
			}
			// Constructing commit instance
			StringBuilder line = new StringBuilder();
			if(bugCommits.contains(commit.getID()))
				line.append(1);
			else
				line.append(1);
			Map<Integer, Integer> features = new TreeMap<Integer, Integer>();
			features.put(getIndex("files_copied"), commit.getFilesCopied());
			features.put(getIndex("log_length"), commitRS.getInt("log_length"));
			features.put(getIndex("changed_LOC"), changedLOC);
			
			Map<String, Integer> termFreq = contentBOW.getTermFreq();
			termFreq.putAll(aDeltaBOW.getTermFreq());
			termFreq.putAll(dDeltaBOW.getTermFreq());
			for(String term : termFreq.keySet()){
				features.put(getIndex(term), termFreq.get(term));
			}
			
			for(ChangeType ct:ChangeType.values()){
				String category = ct.toString();
				Integer count = commit.categorizedChanges.get(category);
				if(count == null)
					count = 0;
				features.put(getIndex(category), count);
			}
			// Sort the indices
			SortedSet<Integer> indices = new TreeSet<Integer>(features.keySet());
			
			for(Integer i : indices){
				line.append(' ');
				line.append(i);
				line.append(':');
				line.append(features.get(i));
			}
			line.append('\n');
			fWriter.write(line.toString());
		}
		fWriter.close();
		//Log attribute indices
		fWriter = new FileWriter("attr_indices.log");
		fWriter.write(attrIndex.toString());
		fWriter.close();
		conn.close();

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
	
	public static int getIndex(String attributeName){
		if(attrIndex == null){
			attrIndex = new TreeMap<String, Integer>();
		}
		Integer index = attrIndex.get(attributeName);
		if(index == null){
			index = attrIndex.size()+1;
			attrIndex.put(attributeName, index);
		}
		return index;
	}
	
}
