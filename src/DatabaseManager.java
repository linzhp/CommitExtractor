

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Properties;

public class DatabaseManager {

    private static DatabaseManager dbManager;
    private String drivername, databasename, username, password;
    private Connection conn = null;
    Statement stmt;

    private DatabaseManager(String props) {
        File file = new File(props);
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            Properties prop = new Properties();
            prop.load(fis);
            //Enumeration enums = prop.propertyNames(); 
            drivername = (String) prop.get("Driver");
            databasename = (String) prop.get("URL");
            username = (String) prop.get("UserName");
            password = (String) prop.get("UserPass");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fis != null)
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }

        try {
            Class.forName(drivername).newInstance();
            conn = DriverManager
                    .getConnection(databasename, username, password);
            stmt = conn.createStatement();

        } catch (Exception e) {
            System.out.println(e);
            System.exit(0);
        }
    }
    
    public static Connection getTestConnection() {
        if (dbManager == null) {
            dbManager = new DatabaseManager("testdatabase.properties");
        }
        return dbManager.conn;
    }

    public static Connection getConnection() {
        if (dbManager == null) {
            dbManager = new DatabaseManager("config.properties");
        }
        return dbManager.conn;
    }

    public static void close() {
        try {
            dbManager.conn.close();
        } catch (Exception e) {
            System.out.println(e);
            System.exit(0);
        }
    }
}