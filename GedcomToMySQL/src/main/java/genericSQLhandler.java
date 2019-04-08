import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

public class genericSQLhandler {
	String srv,uname,pwd,dbname;
	public Connection connect;
	public genericSQLhandler (String server, String username, String password, String database) {
		srv = server;
		uname = username;
		pwd = password;
		dbname = database;
		connect = null;
}
	public void connectMySQL() throws Exception {
        // Setup the connection with the DB
        try {
  //          Class.forName("com.mysql.jdbc.Driver");  // obsolete
            Class.forName("com.mysql.cj.jdbc.Driver");
            String url = "jdbc:mysql://" + srv + "/" + dbname + "?user=" + uname + "&password=" + pwd;
            System.out.println(url);
			connect = DriverManager
//			        .getConnection("jdbc:mysql://10.0.0.3/feedback?"
	//		                + "user=sqluser&password=sqluserpw");
					.getConnection(url);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			 return;
		}
	}
}
