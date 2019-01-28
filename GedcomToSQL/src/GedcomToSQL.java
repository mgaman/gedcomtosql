import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author David Henry
 *
 */
class Individual {
	public int ref = 0;
	public String familyName;
	public String preNames;
	public String birthDate;
	public String birthPlace;
	public String deathDate;
	public String deathPlace;
	public int parentFamily;
	public List ownFamily = new ArrayList();
	public String gender;
	public String comment; 
	public enum eDP {UNKNOWN,BIRTH,DEATH};
	public eDP addingTo = eDP.UNKNOWN;  // which field DATE/PLAC refers to
	public void clear()
	{
		ref = 0;
		preNames = familyName = birthDate = birthPlace = deathDate = deathPlace = comment = null;
		parentFamily = 0;
//		ownFamily = new ArrayList();
		ownFamily.clear();
		gender = null;
		addingTo = eDP.UNKNOWN;
	}
}

class Family {
	public int ref;
	public int father;
	public int mother;
	public int relationship;  // 0 unknown, 1 married
	public List children = new ArrayList();
	public String marriageDate;
	public String marriagePlace;
	public String divorceDate;
	public String divorcePlace;	
	public String comment;
	public enum eDP {UNKNOWN,MARRIAGE,DIVORCE};
	public eDP addingTo = eDP.UNKNOWN;  // which field DATE/PLAC refers to
	public void clear() {
		ref = father = mother = relationship = 0;
		marriageDate = marriagePlace = divorceDate = divorcePlace = comment = null;
		children.clear();
		addingTo = eDP.UNKNOWN;
	}
}

public class GedcomToSQL {

	static Connection conn = null;
	static Statement stmt = null;
	ResultSet rs = null;
	static int gedcomlevel = 0;
	static enum eTypes {UNSET,HEAD,SUBM,INDI,FAM,TRAILER,UNKNOWN};
	static eTypes type = eTypes.UNSET;
	static int lineNumber = 0;
	
	// statistics for sanity checking
	static int indivCount = 0;
	static int indivLines = 0;
	static int famCount = 0;
	static int headCount = 0;
	static int headLines = 0;
	static int submCount = 0;
	static int trlrCount = 0;
	static int unknownCount = 0;
	static int sqlerrors = 0;
	static int indErrors = 0;
	static int famErrors = 0;
	static boolean indivPending = false;
	static boolean famPending = false;
	static Individual currentIndividual = new Individual();
	static Family currentFamily = new Family();
	
	static void processLine(String s)
	{
		// split line at first blank to get level and rest
		// if level 0 next part(s) is top level type
		String st = s.trim();
		int br = st.indexOf(" ");
		if (br > 0)   // last line is CTRL^Z
		{
			int level = Integer.parseInt(st.substring(0, br));
			if (level == 0) // determine kind of toplevel
			{
				if (st.substring(br+1).equals("HEAD"))
				{
					type = eTypes.HEAD;
				}
				else if (st.substring(br+1).equals("TRLR"))
				{
					type = eTypes.TRAILER;
				}
				else {  // others have 2 parts
					String [] parts = st.split(" ");
					if (parts.length == 3) {
						if (parts[2].equals("INDI"))
							type = eTypes.INDI;
						else if (parts[2].equals("SUBM"))
							type = eTypes.SUBM;
						else if (parts[2].equals("FAM"))
							type = eTypes.FAM;
					}
						else
							type = eTypes.UNKNOWN;
				}
				processType(st,type, true);
			}
			else
				processType(st,type, false);
		}
	}

	static private String addQuotes(String s) {
		return s == null ? "NULL" : "'" + s + "'";
	}
	
	/**
	 * String data with embedded single quotes cause problems for SQL INSERT to each
	 * single quote must be escaped with another single quote
	 * @param s
	 * @return
	 */
	static private String escapeQuotes(String s) {
		String reply = "";
		if (s == null)
			return null;
		if (s.indexOf('\'') == -1)
			reply = s;
		else {
			String remainder = s;
			int ind = remainder.indexOf('\'');
			while ( ind != -1) {
				reply += remainder.substring(0,ind);
				reply += "''";
				remainder = remainder.substring(ind + 1);
				ind = remainder.indexOf('\'');
			}
			reply += remainder;
		}
		return reply;
	}
	static private String ListToCSV(List<Integer> l)
	{
		String reply = "";
		for (int i = 0; i < l.size(); i++) {
		    reply += l.get(i) + ",";
		}
		return escapeQuotes(reply.substring(0,reply.length()-1));   // trim last comma
	}
	private static void emitIndividual()
	{
		// Create SQL INSERT, adding all columns so no need to list columns
		String prefix = "Insert into Individual Values (";
		String suffix = ");";
		
		String middle = String.valueOf(currentIndividual.ref) + ",";
		middle += addQuotes(escapeQuotes(currentIndividual.familyName)) + ",";
		middle += addQuotes(escapeQuotes(currentIndividual.preNames)) + ",";
		middle += addQuotes(escapeQuotes(currentIndividual.birthDate)) + ",";
		middle += addQuotes(escapeQuotes(currentIndividual.birthPlace)) + ",";
		middle += addQuotes(escapeQuotes(currentIndividual.deathDate)) + ",";
		middle += addQuotes(escapeQuotes(currentIndividual.deathPlace)) + ",";
		middle += String.valueOf(currentIndividual.parentFamily) + ",";
		if (currentIndividual.ownFamily.isEmpty())
				middle += "NULL,";
		else
			middle += addQuotes(ListToCSV(currentIndividual.ownFamily)) + ",";
		middle += addQuotes(currentIndividual.gender) + ",";
		if (currentIndividual.comment == null)
			middle += "NULL";
		else
			middle += addQuotes(escapeQuotes(currentIndividual.comment));
		String sql = prefix + middle + suffix;
		try {
			stmt.execute(sql);
		} catch (SQLException e) {
			sqlerrors++;
            System.out.println(e.getMessage());
            System.out.println(sql);
            
        }

/*		String sql = "Insert into Individual Values (?,?,?,?,?,?,?,?,?,?,?)";
	       try (//Connection conn = this.connect();
	            PreparedStatement pstmt = conn.prepareStatement(sql)) {
	            pstmt.setInt(1, currentIndividual.ref);
	            pstmt.setString(2, currentIndividual.familyName);
	            pstmt.setString(3, currentIndividual.preNames);
	            pstmt.setString(4, currentIndividual.birthDate);
	            pstmt.setString(5, currentIndividual.birthPlace);
	            pstmt.setString(6, currentIndividual.deathDate);
	            pstmt.setString(7, currentIndividual.deathPlace);
	            pstmt.setInt(8, currentIndividual.parentFamily);
	            pstmt.setString(9, ListToCSV(currentIndividual.ownFamily));
	            pstmt.setString(10, currentIndividual.gender);
	            pstmt.setString(11, ListToCSV(currentIndividual.comments));
	            pstmt.executeUpdate();
	        } catch (SQLException e) {
	            System.out.println(e.getMessage());
	        }
*/
	}
	/*
	 *  We do not know that a record is complete until the reception of a new record with top == true
	 *  So we just accumulate data until next record comes along, then write out it out.
	 */
	public static void processType(String s, eTypes t, boolean top)
	{
		switch (t) {
		case HEAD:
			if (top)
				headCount++;
			break;
		case SUBM:
			if (top)
				submCount++;
			break;
		case TRAILER:
			if (top)
			{
				if (indivPending)
				  emitIndividual();
				if (famPending)
					emitFamily();
				trlrCount++;
			}
			break;
		case UNKNOWN:
			unknownCount++;
			break;
		case INDI:
			if (top)
			{
				if (indivPending)
				{
					indivPending = false;
					emitIndividual();
					currentIndividual.clear();
				}
				indivCount++;
			}
			else
			{
//				indivLines++;
				indivPending = true;
				addToIndividual(s);
			}
			break;
		case FAM:
			if (top)
			{
				if (famPending)
				{
					famPending = false;
					emitFamily();
					currentFamily.clear();
				}
				famCount++;
			}
			// top line contains reference number so must be processed
			famPending = true;
			addToFam(s);
			break;
		default:
			break;
		}
	}
/*	0 @F6@ FAM 
	1 HUSB @I9@ 
	1 WIFE @I10@ 
	1 CHIL @I8@ 
	1 CHIL @I20@ 
	1 CHIL @I18@ 
	1 MARR 
	2 DATE 12 AUG 1906
	2 PLAC Bevis Marks
	2 NOTE blah
*/
	private static void addToFam(String s) {
		int br = s.indexOf(' ');
		String rs = s.substring(br+1);
		if (rs.startsWith("@F"))
		{
			String rest = rs.substring(2);
			int brx = rest.indexOf('@');
			currentFamily.ref = Integer.parseInt(rest.substring(0,brx));
			System.out.println("Adding family " + currentFamily.ref);			
		}
		else if (rs.startsWith("HUSB"))
		{
			String rest = rs.substring(7);  // skip over HUSB @I
			int brx = rest.indexOf('@');
			currentFamily.father = Integer.parseInt(rest.substring(0,brx));			
		}
		else if (rs.startsWith("WIFE"))
		{
			String rest = rs.substring(7);  // skip over HUSB @
			int brx = rest.indexOf('@');
			currentFamily.mother = Integer.parseInt(rest.substring(0,brx));						
		}
		else if (rs.startsWith("CHIL"))
		{
			String rest = rs.substring(7);  // skip over HUSB @
			int brx = rest.indexOf('@');
			currentFamily.children.add(Integer.parseInt(rest.substring(0,brx)));						
		}
		else if (rs.startsWith("MARR"))
		{
			currentFamily.relationship = 1;
			currentFamily.addingTo = Family.eDP.MARRIAGE;
		}
		else if (rs.startsWith("DATE"))
		{
			String rest = rs.substring(5);  // skip over DATE 
			switch (currentFamily.addingTo) {
			case MARRIAGE:
				currentFamily.marriageDate = rest;
				break;
			default:
				break;
			};			
		}
		else if (rs.startsWith("PLAC"))
		{
			String rest = rs.substring(5);  // skip over PLAC 
			switch (currentFamily.addingTo) {
			case MARRIAGE:
				currentFamily.marriagePlace = rest;
				break;
			default:
				break;
			};			
		}
		else if (rs.startsWith("DIV"))
		{
		}
		else if (rs.startsWith("NOTE"))
		{
			String rest = rs.substring(5);  // skip over PLAC 
			currentFamily.comment = rest;
		}
		else if (rs.startsWith("PFAM"))
		{
		}
		else
			famErrors++;
	}

	private static void emitFamily() {
		// TODO Auto-generated method stub
		// Create SQL INSERT, adding all columns so no need to list columns
		String prefix = "Insert into Family Values (";
		String suffix = ");";
		
		String middle = String.valueOf(currentFamily.ref) + ",";
		middle += String.valueOf(currentFamily.father) + ",";
		middle += String.valueOf(currentFamily.mother) + ",";
		middle += String.valueOf(currentFamily.relationship) + ",";
		if (currentFamily.children.isEmpty())
			middle += "NULL,";
		else
			middle += addQuotes(ListToCSV(currentFamily.children)) + ",";
		middle += addQuotes(escapeQuotes(currentFamily.marriageDate)) + ",";
		middle += addQuotes(escapeQuotes(currentFamily.marriagePlace)) + ",";
		middle += addQuotes(escapeQuotes(currentFamily.divorceDate)) + ",";
		middle += addQuotes(escapeQuotes(currentFamily.divorcePlace))+ ",";
		middle += addQuotes(escapeQuotes(currentFamily.comment));
		String sql = prefix + middle + suffix;
		try {
			stmt.execute(sql);
		} catch (SQLException e) {
			sqlerrors++;
            System.out.println(e.getMessage());
            System.out.println(sql);
        }
		
	}

	public static void main(String[] args) {
	//	String x = escapeQuotes("See Philip Afia's notes. Camille hid in a boarded up room during WW2 in ");
	//	x = escapeQuotes("bbc'v'b''ddd");
		// setup database
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

 		try {
			stmt.execute("Delete from Individual;");
		} catch (SQLException e) {
            System.out.println(e.getMessage());
        }
 		try {
			stmt.execute("Delete from Family;");
		} catch (SQLException e) {
            System.out.println(e.getMessage());
        }

		try {
			FileInputStream fstream = new FileInputStream("src/Data/transfer.ged");
			// Get the object of DataInputStream
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			// Read File Line By Line
			while ((strLine = br.readLine()) != null) {
				// Print the content on the console
			//	System.out.println(strLine);
				processLine(strLine);
				lineNumber++;
			}
			// Close the input stream
			in.close();
		} catch (Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
		System.out.println(indivCount + " individuals");
		System.out.println(indivLines + " individuals lines");
		System.out.println(famCount + " families");
		System.out.println(headCount + " heads");
		System.out.println(submCount + " submitters");
		System.out.println(trlrCount + " trailers");
		System.out.println(unknownCount + " unknown");
		System.out.println(sqlerrors  + " SQL errors");
		System.out.println(famErrors + " family untreated");
		System.out.println(indErrors + " indiv untreated");
	}
	
	private static void addToIndividual(String s) {
		// split at first space & check next field
		// cannot split all on space as space also embedded in data e.g. dates, names, places
		int br = s.indexOf(' ');
		String rest = s.substring(br + 1); // start past first blank
//		System.out.println(rest);
		if (rest.startsWith("REFN"))
		{
			int brx = rest.indexOf(' ') + 1;
			currentIndividual.ref = Integer.parseInt(rest.substring(brx));
			System.out.println("Adding ref " + currentIndividual.ref);
		}
		else if (rest.startsWith("NAME")) {
			String name = rest.substring(rest.indexOf(" ")+1);
			String [] names = name.split("/");
			currentIndividual.preNames = names[0];
			if (names.length > 1)
				currentIndividual.familyName = names[1];
		}
		else if (rest.startsWith("BIRT")) {
			currentIndividual.addingTo = Individual.eDP.BIRTH;
		}
		else if (rest.startsWith("DEAT")) {
			currentIndividual.addingTo = Individual.eDP.DEATH;			
		}
		else if (rest.startsWith("DATE")) {
			String date = rest.substring(rest.indexOf(" ")+1);
			switch (currentIndividual.addingTo) {
			case BIRTH:
				currentIndividual.birthDate = date;
				break;
			case DEATH:
				currentIndividual.deathDate = date;
				break;
			default:
				break;
			};
		}
		else if (rest.startsWith("PLAC")) {
			String place = rest.substring(rest.indexOf(" ")+1);
			switch (currentIndividual.addingTo) {
			case BIRTH:
				currentIndividual.birthPlace = place;
				break;
			case DEATH:
				currentIndividual.deathPlace = place;
				break;
			default:
				break;
			};			
		}
		else if (rest.startsWith("FAMC")) {
			String famc = rest.substring(rest.indexOf("@F")+2);
			// cannot parse a string like "5@" causes an exception
			int lasta = famc.indexOf('@');
			currentIndividual.parentFamily = Integer.parseInt(famc.substring(0,lasta));
		}
		else if (rest.startsWith("FAMS")) {
			String fama = rest.substring(rest.indexOf("@F")+2);
			int lasta = fama.indexOf('@');
			currentIndividual.ownFamily.add(Integer.parseInt(fama.substring(0,lasta)));			
		}
		else if (rest.startsWith("NOTE")) {
			String note = rest.substring(rest.indexOf(" ")+1);	
			currentIndividual.comment = note;
		}
		// CONT is a continuation of NOTE
		else if (rest.startsWith("CONT")) {
			String cont = rest.substring(rest.indexOf(" ")+1);	
			currentIndividual.comment += cont;			
		}
		else if (rest.startsWith("SEX")) {
			String sex = rest.substring(rest.indexOf(" ")+1);	
			currentIndividual.gender = sex;		
		}
		else
			indErrors++;
	}
}
