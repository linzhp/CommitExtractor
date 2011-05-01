import java.sql.*;

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
		stmt.executeUpdate("drop table if exists features");
		stmt.executeUpdate("create table features("
				+ "id int primary key auto_increment,"
				+ "commit_id int not null unique" + ")");
		ResultSet commitRS = stmt.executeQuery("select id "
				+ "from scmlog s where s.repository_id=3 limit 10");// FIXME
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
				int file_id = file.getInt("file_id");
				switch (action_type) {
				case 'C': commit.incrFilesCopied(); break;
				case 'M': commit.processModify(file_id); break;
				case 'D': commit.processDelete(); break;
				case 'A': commit.processAdd(); break;
				case 'V': commit.processRename();break;
				}
			}
		}
		conn.close();
	}

}
