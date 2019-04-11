import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;
import org.json.simple.parser.JSONParser;
import org.json.simple.JSONArray;

public class GetFamilyData {
	genericSQLhandler sqlHandler;
	public boolean debug;  // print statements
	List<Integer> levelsCount ;  // count of entries at each level
	List<treeEntry> nodes;
	int descendants = 0;
	/*
	 *  The database tables include arrays as json variables. As I have not
	 *  yet learned the json syntax of mySQL I am doing stuff the long way
	 */
	private List<Long> getChildren(long head) {
			String kids = null;
			List<Long> lKids = new ArrayList<Long>();
			List<Long> lRel = new ArrayList<Long>();
			PreparedStatement preparedStatement = null;
		    ResultSet familiesResultSet = null;
		    ResultSet childrenResultSet = null;
		    JSONParser parser = new JSONParser();
		    int i,j;
		    
			try {
				preparedStatement = sqlHandler.connect.prepareStatement
				        ("select Relationships from Individual where id=?");
				preparedStatement.setLong(1, head);
	            familiesResultSet = preparedStatement.executeQuery();
	            while (familiesResultSet.next()) {
		            String families = familiesResultSet.getString("Relationships");
		            if (families == null) {
		            	if (debug) {
		            		System.out.println(head + ": Relationships: None");
		            	}		            	
		            }
		            else {
		            	if (debug) {
		            		System.out.println(head + ": Relationships: " + families);
		            	}
		   	         	Object obj = parser.parse(families);
		   	         	JSONArray array = (JSONArray)obj;
		   	         	for (i=0;i<array.size();i++) {
		   	        			lRel.add((Long) array.get(i));
		   	         	}
			            // now we have relationships, go to relationship to get children
		   	         	for (i=0;i<lRel.size();i++) {
		   	         		preparedStatement = sqlHandler.connect.prepareStatement
						        ("select children from Family where ID=?");
		   	         		preparedStatement.setLong(1,lRel.get(i));
		   	         		childrenResultSet = preparedStatement.executeQuery();
		   		            while (childrenResultSet.next()) {
		   			            String children = childrenResultSet.getString("children");
		   			            if (children == null) {
			   		            	if (debug) {
			   		            		System.out.println(lRel.get(i) + ": Children: None");
			   		            	}
		   			            }
		   			            else {
			   		            	if (debug) {
			   		            		System.out.println(lRel.get(i) + ": Children " + children);
			   		            	}
			   		   	         	Object obj2 = parser.parse(children);
			   		   	         	JSONArray array2 = (JSONArray)obj2;
			   		   	         	if (array2.size()>0)
			   		   	         		for (j=0;j<array2.size();j++) {
			   		   	         			lKids.add((Long) array2.get(j));
			   		   	         	}
		   			            }
		   		            }
		   	         	}
		            }
	            }
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
			return lKids;
	}
			
	public GetFamilyData(genericSQLhandler gsh) {  // constructor
		 sqlHandler = gsh;
		 debug = false;
		 levelsCount = new ArrayList<Integer>();
		 nodes = new ArrayList<treeEntry>();
		 try {
			 sqlHandler.connectMySQL();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public List<treeEntry> getDescendants(long head, int level) {
		// dynamically grow list of levels
		if (levelsCount.isEmpty() || levelsCount.size() < level+1)
			levelsCount.add(0);
		levelsCount.set(level, levelsCount.get(level)+1);  // bump count at this level
		List<Long> kids = getChildren(head);
		if (!kids.isEmpty()) {
			int NewLevel = level+1;
			for (int i=0; i< kids.size(); i++) {
				if (debug)
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
		return nodes;
	}

	public void getAscendants(int start, int maxlevel) {
	}
	
	public Individual getIndividual(long id) {
		PreparedStatement preparedStatement = null;
	    ResultSet resultSet = null;
	    Individual indiv = new Individual();
	    JSONParser parser = new JSONParser();
	    int i;
	    
		try {
			preparedStatement = sqlHandler.connect.prepareStatement
			        ("select * from Individual where id=?");
			preparedStatement.setLong(1, id);
	        resultSet = preparedStatement.executeQuery();
	        while (resultSet.next()) {
	        	indiv.id = resultSet.getInt("ID");
	        	indiv.BirthFamilyName = resultSet.getString("BirthFamilyName");
	        	String forenames = resultSet.getString("ForeNames");
	        	// convert JSON to list
   	         	Object obj = parser.parse(forenames);
   	         	JSONArray array = (JSONArray)obj;
   	         	for (i=0;i<array.size();i++) {
   	        			indiv.ForeNames.add(array.get(i));
   	         	}	        	
	        }
	        // copy data to the class instance
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return indiv;
	}
}
