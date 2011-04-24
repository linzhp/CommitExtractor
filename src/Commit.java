import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.*;
import java.util.logging.*;

import org.diffxml.diffxml.DOMOps;
import org.diffxml.diffxml.Diff;
import org.diffxml.diffxml.DiffFactory;
import org.diffxml.diffxml.xmdiff.XmDiff;
import org.w3c.dom.Document;


public class Commit {
	private int filesCopied = 0;
	private int id;
	private Logger logger;
	
	public int getID() {
		return id;
	}

	public Commit(int commitID){
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
	
	public void incrFilesCopied(){
		filesCopied += 1;
	}
	
	public void processModify(int fileID) throws SQLException{
		Connection conn = DatabaseManager.getConnection();
		Statement stmt = conn.createStatement();
		String query = "select content from content where file_id="+fileID+" and commit_id="+this.id;
		logger.fine(query);
		ResultSet rs = stmt.executeQuery(query);
		if(!rs.next())
		{
			logger.config("Content for file "+fileID+" at commit_id "+id+" not found");
			return;
		}
		String newContent = rs.getString("content");
		rs = stmt.executeQuery("select content " +
				"from content where file_id="+fileID+" and commit_id<"+this.id+
				" order by commit_id desc limit 1");
		if(!rs.next())
		{
			logger.config("No content for previous version of "+fileID+" at commit_id "+id+" found");
			return;
		}
		String oldContent = rs.getString("content");
		try {
			src2XML(newContent, "new.xml");
			src2XML(oldContent, "old.xml");
			Diff diff = DiffFactory.createDiff();
			File oldXML = new File("old.xml");
			File newXML = new File("new.xml");
			Document delta = diff.diff(oldXML, newXML);
			DOMOps.outputXMLIndented(delta, System.out);
			oldXML.delete();
			newXML.delete();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}
	
	private void src2XML(String source, String fileName) throws IOException, InterruptedException{
		File tempFile = new File("temp.java");
		FileWriter fWriter = new FileWriter(tempFile);
		fWriter.write(source);
		fWriter.close();
		Runtime rt = Runtime.getRuntime();
		Process ps = rt.exec("src2srcml -l Java temp.java -o "+fileName);
		int exit=ps.waitFor();
		logger.config("src2srcml exit with "+exit);
		tempFile.delete();
	}
	
	public void processDelete(){
		
	}
	
	public void processAdd(){
		
	}
	
	public void processRename(){
		
	}
}
