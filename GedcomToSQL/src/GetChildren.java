import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

class treeEntry {
	public int ID;  // into Individuals table
	public int level; // top of tree is level 0
	public int parent; 
}

public class GetChildren {
	static Connection conn = null;
	static Statement stmt = null;
	ResultSet rs = null;
	static int descendants = 0;
	static List<Integer> levelsCount = new ArrayList<Integer>();  // count of entries at each level
	static List<treeEntry> nodes = new ArrayList<treeEntry>();
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			Class.forName("org.sqlite.JDBC");
			conn = DriverManager.getConnection("jdbc:sqlite:src/Data/family.db");
			System.out.println("Opened database successfully");
			conn.setAutoCommit(true);
			stmt = conn.createStatement();
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
		//System.out.println(getChildren(1));
		getDescendants(11,0);
		System.out.println("Total descendants " + descendants);
		// print count at each level
		for (int i=0;i<levelsCount.size();i++)
			System.out.println("Level "+ i + ",Count "+ levelsCount.get(i));
		
	}
	
	static private List<Integer> getChildren(int head) {
		String sql = "select children from family where id=(select ownfamily from individual where id=" + 
	        head + ")";
		String kids = null;
		List<Integer> lKids = new ArrayList<Integer>();
		try (
	        Statement stmt  = conn.createStatement();
	        ResultSet rs    = stmt.executeQuery(sql)){
	            
	        // loop through the result set
        while (rs.next()) {
       // 	System.out.println(rs.getString("children"));
        	kids = rs.getString("children");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
		if (kids != null) {
			String [] parts = kids.split(",");
			for (int i=0;i<parts.length;i++)
			{
				lKids.add(Integer.parseInt(parts[i]));
			}
		}
		return lKids;
	}
	static private void getDescendants(int head, int level) {
		// dynamically grow list of levels
		if (levelsCount.isEmpty() || levelsCount.size() < level+1)
			levelsCount.add(0);
		levelsCount.set(level, levelsCount.get(level)+1);  // bump count at this level
		List<Integer> kids = getChildren(head);
		if (!kids.isEmpty()) {
			int NewLevel = level+1;
			for (int i=0; i< kids.size(); i++) {
				System.out.println("ID " + kids.get(i) + ",Parent " + head + ",Level " + NewLevel);
				treeEntry te = new treeEntry();
				te.ID = kids.get(i);
				te.parent = head;
				te.level = NewLevel;
				nodes.add(te);
				descendants++;
				getDescendants(kids.get(i),NewLevel);
			}
		}			
	}
}
