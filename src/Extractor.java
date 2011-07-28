import java.io.File;
import java.io.FileInputStream;
import java.sql.*;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Properties;

import org.evolizer.changedistiller.model.classifiers.ChangeType;

import weka.BOWPlusTokenizer;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.NumericToNominal;
import weka.filters.unsupervised.attribute.StringToWordVector;

public class Extractor {


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
		HashSet<Integer> bugCommits = new HashSet<Integer>();
		ResultSet bugCommitRS = stmt.executeQuery("select distinct bug_commit_id from" +
				" hunk_blames where bug_commit_id in " +
				"(select id from scmlog where repository_id="+repoID+")");
		while(bugCommitRS.next()){
			bugCommits.add(bugCommitRS.getInt("bug_commit_id"));
		}
		
		// Fetching commit data
		ResultSet commitRS = stmt.executeQuery("select s.id, author_id, author_date, length(message) as log_length "
				+ "from scmlog s where s.repository_id="+repoID+" or s.repository_id=2 limit 10");//TODO remove limit
		Instances rawData = getInstances();
		Attribute authorIDAttr = rawData.attribute("author_id");
		Attribute commitHourAttr = rawData.attribute("commit_hour");
		Attribute commitDayAttr = rawData.attribute("commit_day");
		Attribute newSourceAttr = rawData.attribute("new_source");
		Attribute addedDeltaAttr = rawData.attribute("added_delta");
		Attribute deletedDeltaAttr = rawData.attribute("deleted_delta");
		while (commitRS.next()) {
			Commit commit = new Commit(commitRS.getInt(1));
			Instance commitInst = new Instance(rawData.numAttributes());
			// Generating ASF diff data
			Statement stmt1 = conn.createStatement();
			ResultSet file = stmt1
					.executeQuery("select * " +
							"from action_files af join files f on af.file_id=f.id " +
							"where commit_id="+commit.getID() +
							" and f.file_name like '%.java'");
			StringBuilder newSource = new StringBuilder();
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
				newSource.append(commit.getNewContent(fileID));
			}
			//Getting source code delta
			ResultSet rs = stmt1.executeQuery("select patch from patches where commit_id="+commit.getID());
			rs.next();
			String patch = rs.getString(1);
			String addedDelta = extractDelta(patch, '+');
			String deletedDelta = extractDelta(patch, '-');
			// Getting commit meta data
			int authorID = commitRS.getInt("author_id");
			Timestamp date = commitRS.getTimestamp("author_date");
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			int hour = cal.get(Calendar.HOUR_OF_DAY);
			int day = cal.get(Calendar.DAY_OF_WEEK);
			
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
			if(bugCommits.contains(commit.getID()))
				commitInst.setValue(rawData.attribute("buggy"), 1);
			else
				commitInst.setValue(rawData.attribute("buggy"), 0);
			commitInst.setValue(rawData.attribute("files_copied"), commit.getFilesCopied());
			commitInst.setValue(authorIDAttr, authorID);
			commitInst.setValue(commitHourAttr, hour);
			commitInst.setValue(commitDayAttr, day);
			commitInst.setValue(rawData.attribute("log_length"), commitRS.getInt("log_length"));
			commitInst.setValue(rawData.attribute("changed_LOC"), changedLOC);
			
			commitInst.setValue(newSourceAttr, newSource.toString());
			commitInst.setValue(addedDeltaAttr, addedDelta);
			commitInst.setValue(deletedDeltaAttr, deletedDelta);

			for(ChangeType ct:ChangeType.values()){
				String category = ct.toString();
				Integer count = commit.categorizedChanges.get(category);
				if(count == null)
					count = 0;
				commitInst.setValue(rawData.attribute(category), count);
			}
			rawData.add(commitInst);
		}
		conn.close();
		// Convert some numeric attributes to nominal
		NumericToNominal numericToNominal = new NumericToNominal();
		StringBuilder indices = new StringBuilder();
		indices.append(rawData.classIndex()+1);
		indices.append(',');
		indices.append(authorIDAttr.index()+1);
		indices.append(',');
		indices.append(commitHourAttr.index()+1);
		indices.append(',');
		indices.append(commitDayAttr.index()+1);
		String[] options = {"-R",indices.toString()};
		numericToNominal.setOptions(options);
		numericToNominal.setInputFormat(rawData);
		rawData = Filter.useFilter(rawData, numericToNominal);
		
		rawData = stringToVector(rawData, "new_source");
		rawData = stringToVector(rawData, "added_delta");
		rawData = stringToVector(rawData, "deleted_delta");
		
		ArffSaver saver = new ArffSaver();
		saver.setInstances(rawData);
		saver.setFile(new File("output.arff"));
		saver.writeBatch();
//		System.out.println(rawData);
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
	
	public static Instances stringToVector(Instances rawData, String attr) throws Exception{
		StringToWordVector stringToWordVector = new StringToWordVector(); 
		stringToWordVector.setOptions(new String[]{"-R", String.valueOf(rawData.attribute(attr).index()+1), 
				"-C", "-W", "10000", 
				"-P", attr,
				"-tokenizer", BOWPlusTokenizer.class.getName()});
		stringToWordVector.setInputFormat(rawData);
		return Filter.useFilter(rawData, stringToWordVector);
	}
		
	public static Instances getInstances(){
		// Defining ARFF schema
		FastVector attrs = new FastVector();
		Attribute buggy = new Attribute("buggy");
		attrs.addElement(buggy);
		attrs.addElement(new Attribute("files_copied"));
		attrs.addElement(new Attribute("author_id"));
		attrs.addElement(new Attribute("commit_hour"));
		attrs.addElement(new Attribute("commit_day"));
		attrs.addElement(new Attribute("log_length"));
		attrs.addElement(new Attribute("changed_LOC"));
		
		attrs.addElement(new Attribute("added_delta", (FastVector)null));
		attrs.addElement(new Attribute("deleted_delta", (FastVector)null));
		attrs.addElement(new Attribute("new_source", (FastVector)null));

		// AST diff features
		for(ChangeType ct:ChangeType.values()){
			attrs.addElement(new Attribute(ct.toString()));
		}
		Instances inst = new Instances("Raw", attrs, 0);
		inst.setClass(buggy);
		return inst;
	}
}
