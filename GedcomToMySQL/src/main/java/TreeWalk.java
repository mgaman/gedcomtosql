import java.util.ArrayList;
import java.util.List;

public class TreeWalk {

	public static void main(String[] args) {
		genericSQLhandler mh = new genericSQLhandler("10.0.0.3", "waterman", "qvwbenrm", "Family");
		System.out.println("DB Opened");
		GetGenerations gg = new GetGenerations(mh);
//		gg.debug = true;
		
		gg.getDescendants(11,0);
		try {
			mh.disconnectMySQL();
			System.out.println("DB Closed");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
