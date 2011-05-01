import java.io.IOException;
import java.sql.*;
import java.util.List;
import java.util.logging.*;

import org.evolizer.changedistiller.distilling.Distiller;
import org.evolizer.changedistiller.model.entities.SourceCodeChange;

public class Commit {
	private int filesCopied = 0;
	private int id;
	private Logger logger;

	public int getID() {
		return id;
	}

	public Commit(int commitID) {
		this.id = commitID;
		logger = Logger.getLogger(this.getClass().getName());
		FileHandler fh;
		try {
			fh = new FileHandler("extractor.log");
			fh.setFormatter(new SimpleFormatter());
			logger.addHandler(fh);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		logger.setLevel(Level.CONFIG);
	}

	public int getFilesCopied() {
		return filesCopied;
	}

	public void incrFilesCopied() {
		filesCopied += 1;
	}

	public void processModify(int fileID) throws Exception {
		Connection conn = DatabaseManager.getConnection();
		Statement stmt = conn.createStatement();
		String query = "select content from content where file_id=" + fileID
				+ " and commit_id=" + this.id;
		logger.fine(query);
		ResultSet rs = stmt.executeQuery(query);
		if (!rs.next()) {
			logger.config("Content for file " + fileID + " at commit_id " + id
					+ " not found");
			return;
		}
		String newContent = rs.getString("content");
		rs = stmt.executeQuery("select content "
				+ "from content where file_id=" + fileID + " and commit_id<"
				+ this.id + " order by commit_id desc limit 1");
		if (!rs.next()) {
			logger.config("No content for previous version of " + fileID
					+ " at commit_id " + id + " found");
			return;
		}
		String oldContent = rs.getString("content");
		Distiller distiller = new Distiller();
		File newFile = new File();
		File oldFile = new File();
		newFile.setContentString(newContent);
		oldFile.setContentString(oldContent);
		distiller.performDistilling(oldFile, newFile);
		List<SourceCodeChange> changes = distiller.getSourceCodeChanges();
		for (SourceCodeChange c : changes) {
			System.out.println(c.getLabel());
		}
	}

	public void processDelete() {

	}

	public void processAdd() {

	}

	public void processRename() {

	}
}
