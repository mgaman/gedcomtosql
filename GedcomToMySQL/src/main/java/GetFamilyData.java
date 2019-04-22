import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


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
	@SuppressWarnings("unchecked")
	private List<Long> getChildren(long head) {
		List<Long> lKids = new ArrayList<Long>();
	    Family tempFamily;
	    Individual tempIndividual;
	    int i;
	    
		tempIndividual = getIndividual(head);
		if (tempIndividual.ownFamilies.size() > 0)
		{
         	for (i=0;i<tempIndividual.ownFamilies.size();i++) {
         		tempFamily = getFamily((Long) tempIndividual.ownFamilies.get(i));
         		lKids.addAll(tempFamily.children);
         	}		   	         		
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
   	         	indiv.gender = Individual.eGender.valueOf(resultSet.getString("Gender"));
	        	String relationships = resultSet.getString("Relationships");
	        	if (relationships != null) {
		        	// convert JSON to list
	   	         	Object obj2 = parser.parse(relationships);
	   	         	JSONArray array2 = (JSONArray)obj2;
	   	         	for (i=0;i<array2.size();i++) {
	   	        			indiv.ownFamilies.add(array2.get(i));
	   	         	}	
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
	
	public Individual.eGender getGender(long id) {
		PreparedStatement preparedStatement = null;
	    ResultSet resultSet = null;
	    Individual.eGender gender = Individual.eGender.OTHER; 
	    try {
	    	preparedStatement = sqlHandler.connect.prepareStatement
			        ("select Gender from Individual where id=?");
			preparedStatement.setLong(1, id);
	        resultSet = preparedStatement.executeQuery();
	        while (resultSet.next()) {
	        	// jdbc doesnt support enums
	        	gender = Individual.eGender.valueOf(resultSet.getString("Gender"));
	        }
	    }catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return gender;
	}
	public Family getFamily(long id) {
		PreparedStatement preparedStatement = null;
	    ResultSet resultSet = null;
	    Family family = new Family();
	    JSONParser parser = new JSONParser();
		List<Long> lChildren = new ArrayList<Long>();
	    int i,j;

	    try {
			preparedStatement = sqlHandler.connect.prepareStatement
			        ("select * from Family where id=?");
			preparedStatement.setLong(1, id);
	        resultSet = preparedStatement.executeQuery();
	        while (resultSet.next()) {
	        	family.ref = resultSet.getInt("ID");
	        	family.father = resultSet.getInt("father");
	        	family.mother = resultSet.getInt("mother");
	        	family.relationship = Family.eRL.valueOf(resultSet.getString("relationship"));
	            String children = resultSet.getString("children");
	            if (children == null) {
	            	if (debug) {
	            		System.out.println("ID: " + id + " Children: None");
	            	}
	            }
	            else {
	            	if (debug) {
	            		System.out.println("ID: " + id + ": Children " + children);
	            	}
	   	         	Object obj = parser.parse(children);
	   	         	JSONArray array = (JSONArray)obj;
	   	         	if (array.size()>0) {
	   	         		
	   	         		for (j=0;j<array.size();j++) {
	   	         			lChildren.add((Long) array.get(j));
		   	         	}
	   	         		family.children = lChildren;
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

	    return family;
	}
	public Family.eRL getRelationship(long id) {
		return null;
	}

}
