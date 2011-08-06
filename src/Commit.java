import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.logging.*;

import org.evolizer.changedistiller.distilling.Distiller;
import org.evolizer.changedistiller.model.entities.SourceCodeChange;

public class Commit {
	private int filesCopied = 0;
	private int id;
	private Logger logger;
	public HashMap<String, Integer> categorizedChanges = new HashMap<String, Integer>();

	public int getID() {
		return id;
	}

	public Commit(int commitID) {
		this.id = commitID;
		logger = MyLogger.getLogger();
	}

	public int getFilesCopied() {
		return filesCopied;
	}

	public void incrFilesCopied() {
		filesCopied += 1;
	}

	public void processModify(int fileID) throws Exception {
		String newContent = getNewContent(fileID);
		String oldContent = getOldContent(fileID);
		extractDiff(oldContent, newContent, fileID);
	}

	public String getNewContent(int fileID) throws Exception {
		
		Connection conn = DatabaseManager.getConnection();
		Statement stmt = conn.createStatement();
		String query = "select content from content where file_id=" + fileID
				+ " and commit_id=" + this.id;
		logger.fine(query);
		ResultSet rs = stmt.executeQuery(query);
		String result;
		if (!rs.next()) {
			logger.config("Content for file " + fileID + " at commit_id " + id
					+ " not found");
			result = null;
		}else{
			result = rs.getString("content");
		}
		stmt.close();
		return result;
	}

	public void processDelete(int fileID) throws Exception {
		String oldContent = getOldContent(fileID);
		extractDiff(oldContent, "", fileID);
	}

	public void processAdd(int fileID) throws Exception{
		String newContent = getNewContent(fileID);
		extractDiff("", newContent, fileID);
	}

	public void processRename(int fileID) throws Exception {
		processModify(fileID);
	}
	
	private void extractDiff(String oldContent, String newContent, int fileID) {
		if(newContent == null){
			return;
		}
		if(oldContent == null){
			return;
		}
		Distiller distiller = new Distiller();
		File newFile = new File();
		File oldFile = new File();
		newFile.setContentString(newContent);
		oldFile.setContentString(oldContent);
		distiller.performDistilling(oldFile, newFile);
		List<SourceCodeChange> changes = distiller.getSourceCodeChanges();
		if(changes==null){
			logger.config("No diff of file "+fileID+" at commit "+id+" found");
		}else{
			for (SourceCodeChange c : changes) {
				String category = c.getLabel();
				Integer count = categorizedChanges.get(category);
				if(count == null){
					count = 0;
				}
				count++;
				categorizedChanges.put(category, count);
			}			
		}		
	}
	
	private String getOldContent(int fileID) throws Exception{
		Connection conn = DatabaseManager.getConnection();
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery("select content "
				+ "from content where file_id=" + fileID + " and commit_id<"
				+ this.id + " order by commit_id desc limit 1");
		String result;
		if (!rs.next()) {
			logger.config("No content for previous version of " + fileID
					+ " at commit_id " + id + " found");
			result = null;
		}else{
			result = rs.getString("content");
		}
		stmt.close();
		return result;
	}
}
