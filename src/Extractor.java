import java.io.FileInputStream;
import java.sql.*;
import java.util.Calendar;
import java.util.Properties;

import org.evolizer.changedistiller.model.classifiers.ChangeType;

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

		// Create table
//		stmt.executeUpdate("drop table if exists features");
		String createTable = "create table if not exists features("
				+ "id integer primary key auto_increment,"
				+ "commit_id int not null unique," +
						"buggy bool default false," +
						"file_copied int default 0," +
						"author_id int," +
						"commit_hour int," +
						"commit_day int," +
						"log_length int default 0," +
						"changed_LOC int default 0";
		for(ChangeType ct:ChangeType.values()){
			createTable+=","+ct.toString()+" int default 0";
		}
		createTable += ")";
		stmt.executeUpdate(createTable);
		Properties prop = new Properties();
		prop.load(new FileInputStream("config.properties"));
		String repoID=prop.getProperty("RepositoryID");
		ResultSet commitRS = stmt.executeQuery("select id, author_id, date, length(message) as log_length "
				+ "from scmlog s where s.repository_id="+repoID);
		while (commitRS.next()) {
			Commit commit = new Commit(commitRS.getInt(1));
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
			}
			int authorID = commitRS.getInt("author_id");
			Timestamp date = commitRS.getTimestamp("date");
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
			
			StringBuffer attrs = new StringBuffer("insert into features(" +
					"commit_id, file_copied, author_id, commit_hour, commit_day, log_length, changed_LOC");
			StringBuffer values = new StringBuffer("values("+
					commit.getID()+","+commit.getFilesCopied()+","+authorID+","+hour+","+
					day+","+commitRS.getInt("log_length")+","+changedLOC);
			for(String category:commit.categorizedChanges.keySet()){
				attrs.append(","+category);
				values.append(","+commit.categorizedChanges.get(category));
			}
			stmt1.executeUpdate(attrs.append(")").append(values).append(")").toString());
		}
		conn.close();
	}

}
