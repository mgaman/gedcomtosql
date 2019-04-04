import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

public class mySQLhandler {
	String srv,uname,pwd,dbname;
	Connection connect;
	public mySQLhandler (String server, String username, String password, String database) {
		srv = server;
		uname = username;
		pwd = password;
		dbname = database;
		connect = null;
}
	public Connection openDB() throws Exception {
        // Setup the connection with the DB
        try {
  //          Class.forName("com.mysql.jdbc.Driver");
            Class.forName("com.mysql.cj.jdbc.Driver");
			connect = DriverManager
			        .getConnection("jdbc:mysql://10.0.0.3/feedback?"
			                + "user=sqluser&password=sqluserpw");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			 return connect;
		}
	}
}
