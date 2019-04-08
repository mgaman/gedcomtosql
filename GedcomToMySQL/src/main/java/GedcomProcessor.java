import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class GedcomProcessor {

	enum eTypes {UNSET,HEAD,SUBM,INDI,FAM,TRAILER,UNKNOWN};
	eTypes type = eTypes.UNSET;
	 int indivCount = 0;
	 int indivLines = 0;
	 int famCount = 0;
	 int headCount = 0;
	 int headLines = 0;
	 int submCount = 0;
	 int trlrCount = 0;
	 int unknownCount = 0;
	 int sqlerrors = 0;
	 int indErrors = 0;
	 int famErrors = 0;
	 boolean indivPending = false;
	 boolean famPending = false;
	 Individual currentIndividual = new Individual();
	 Family currentFamily = new Family();
	 List<String> Months;
	 
	 genericSQLhandler sqlHandler;
	 
	 public GedcomProcessor(genericSQLhandler gsh)  // constructor
	 {
		 sqlHandler = gsh;
		 try {
			 sqlHandler.connectMySQL();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	 }
	 
	 private String gedcomToSqlDate(String gcdate) {
			List<String> Months = new ArrayList<String>();
			Months.add("JAN");
			Months.add("FEB");
			Months.add("MAR");
			Months.add("APR");
			Months.add("MAY");
			Months.add("JUN");
			Months.add("JUL");
			Months.add("AUG");
			Months.add("SEP");
			Months.add("OCT");
			Months.add("NOV");
			Months.add("DEC");
		String [] parts = gcdate.split(" ");
		// Many variants, nothing, year only, d/mm/y  where mm is 3 letter code
		// convert to SQL yyyy-mm-dd
		String month = "1";
		String day = "1";
		String year = "";
		int mm;
		switch (parts.length)
		{
			case 1:   // year only
				year = parts[0];
				break;
			case 2:   // month + year
				year = parts[1];
				mm = 1;
				if (Months.contains(parts[0])) {
					mm = (Months.indexOf(parts[0]) + 1);
				}
				month = String.format("%d", mm);
				break;
			case 3:  // day + month + year
				mm = 1;
				if (Months.contains(parts[1])) {
					mm = (Months.indexOf(parts[1]) + 1);
				}
				month = String.format("%d", mm);
				day = parts[0];
				year = parts[2];
				break;
		}
		return String.format("%s-%s-%s", year,month,day);
	 }
	/**
	 * First level of parsing for each line from the gedcom file. Looks for a level 0 tag
	 * @param line
	 */
	public void processLine(String line)
	{
		// split line at first blank to get level and rest
		// if level 0 next part(s) is top level type
		String st = line.trim();
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
	
	/**
	 *  We do not know that a record is complete until the reception of a new record with top == true
	 *  So we just accumulate data until next record comes along, then write out it out.
	 * 
	 * @param line - the line of text from the GEDCOM file
	 * @param gedcomType - Gedcom level 0 tag type relevant to this line
	 * @param top - True if this line is a level 0 tag, else false 
	 */
	void processType(String line, eTypes gedcomType, boolean top)
	{
		switch (gedcomType) {
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
				addToIndividual(line);
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
			addToFam(line);
			break;
		default:
			break;
		}
	}
	
	private void addToIndividual(String line) {
		// split at first space & check next field
		// cannot split all on space as space also embedded in data e.g. dates, names, places
		int br = line.indexOf(' ');
		String rest = line.substring(br + 1); // start past first blank
//		System.out.println(rest);
		if (rest.startsWith("REFN"))
		{
			int brx = rest.indexOf(' ') + 1;
			currentIndividual.id = Integer.parseInt(rest.substring(brx));
			//System.out.println("Adding ref " + currentIndividual.id);
		}
		else if (rest.startsWith("NAME")) {
			String name = rest.substring(rest.indexOf(" ")+1);
			String [] names = name.split("/");
			// split firstname into an array
			String [] fnames = names[0].split(" ");
			// if field empty set both firstname and middlenames to empty
			for (int i=0;i<fnames.length;i++)
				currentIndividual.ForeNames.add(fnames[i]);
			if (names.length > 1)
			{
				// GEDCOM doesnt allow for previous names so I put them in ()
				// here I split them up into current and previous names
				if (!names[1].contains("("))
						currentIndividual.BirthFamilyName= names[1];
				else
				{
					String [] pnames = names[1].split("\\(");
					currentIndividual.BirthFamilyName = pnames[0].trim();
					// ( not always closed by )
					currentIndividual.OtherFamilyNames.add(pnames[1].trim());
				}
			}
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
			currentIndividual.ownFamilies.add(Integer.parseInt(fama.substring(0,lasta)));			
		}
		else if (rest.startsWith("NOTE")) {
			String note = rest.substring(rest.indexOf(" ")+1);	
			currentIndividual.comment = note;
		}
		// CONT is a continuation of NOTE
		else if (rest.startsWith("CONT")) {
			String cont = rest.substring(rest.indexOf(" ")+1);	
			currentIndividual.comment += " " + cont;			
		}
		else if (rest.startsWith("SEX")) {
			String sex = rest.substring(rest.indexOf(" ")+1);
			if (sex.equals("M"))
				currentIndividual.gender = Individual.eGender.Male;		
			else if (sex.equals("F"))
				currentIndividual.gender = Individual.eGender.Female;		
		}
		else
			indErrors++;
	}

	private void addToFam(String s) {
		int br = s.indexOf(' ');
		String rs = s.substring(br+1);
		if (rs.startsWith("@F"))
		{
			String rest = rs.substring(2);
			int brx = rest.indexOf('@');
			currentFamily.ref = Integer.parseInt(rest.substring(0,brx));
			//System.out.println("Adding family " + currentFamily.ref);			
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
			currentFamily.relationship = Family.eRL.Married;
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

	public void printStats() {
		System.out.println(indivCount + " individuals");
	//	System.out.println(indivLines + " individuals lines");
		System.out.println(famCount + " families");
		System.out.println(headCount + " heads");
		System.out.println(submCount + " submitters");
		System.out.println(trlrCount + " trailers");
		System.out.println(unknownCount + " unknown");
		System.out.println(sqlerrors  + " SQL errors");
		System.out.println(famErrors + " family unprocessed");
		System.out.println(indErrors + " indiv untreated");
	}
	/**
	 * String data with embedded single quotes cause problems for SQL INSERT to each
	 * single quote must be escaped with another single quote
	 * @param s
	 * @return
	 */
	private String escapeQuotes(String s) {
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
	
	/*
	 *  All JSON stuff here is just Json Array so we can make the string ourselves and insert that
	 */
	void emitFamily()
	{
	    PreparedStatement preparedStatement = null;
	    System.out.println(String.format("Family: %d",currentFamily.ref));
	    String temp;
        try {
			preparedStatement = sqlHandler.connect.prepareStatement
			        ("insert into Family values (?,?,?,?,?,?,?,?,?,?)");
			preparedStatement.setInt(1, currentFamily.ref);
			preparedStatement.setInt(2, currentFamily.father);
			preparedStatement.setInt(3, currentFamily.mother);
			preparedStatement.setString(4,"Married") ;
			if (currentFamily.children.size() == 0)
			{
				preparedStatement.setNull(5, Types.VARCHAR);
			}
			else {
				temp = "[";
				for (int i=0; i<currentFamily.children.size();i++) {
					temp += currentFamily.children.get(i) + ",";
					}
				temp = temp.substring(0,temp.length()-1) + "]";
//				System.out.println(temp);
				preparedStatement.setString(5,temp) ;
			}
			if (currentFamily.marriageDate == null ) {
				preparedStatement.setNull(6,Types.DATE);
			}
			else {
				preparedStatement.setString(6,gedcomToSqlDate(currentFamily.marriageDate));
			}
			preparedStatement.setString(7,currentFamily.marriagePlace);
			if (currentFamily.divorceDate == null ) {
				preparedStatement.setNull(8,Types.DATE);
			}
			else {
				preparedStatement.setString(8,gedcomToSqlDate(currentFamily.divorceDate));
			}
			preparedStatement.setString(9,currentFamily.divorcePlace);
			preparedStatement.setString(10,currentFamily.comment);
            preparedStatement.executeUpdate();				
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	void emitIndividual() {
	    PreparedStatement preparedStatement = null;
	    System.out.println(String.format("Indiv: %d",currentIndividual.id));
	    String temp;
	    int i;
	    try {
			preparedStatement = sqlHandler.connect.prepareStatement
			        ("insert into Individual values (?,?,?,?,?,?,?,?,?,?,?,?)");
			preparedStatement.setInt(1,currentIndividual.id);
			
			if (currentIndividual.BirthFamilyName.equals(null))
				preparedStatement.setNull(2, Types.VARCHAR);
			else
				preparedStatement.setString(2, currentIndividual.BirthFamilyName);
			
			if (currentIndividual.OtherFamilyNames.size() == 0)
				preparedStatement.setNull(3, Types.VARCHAR);
			else {
				temp = "[";
				for (i=0;i<currentIndividual.OtherFamilyNames.size();i++) {
					temp += String.format("\"%s\",", escapeQuotes((String)currentIndividual.OtherFamilyNames.get(i)));
				}
				temp = temp.substring(0,temp.length()-1) + "]";
				preparedStatement.setString(3,temp);
			}
			
			if (currentIndividual.ForeNames.size() == 0)
				preparedStatement.setNull(4, Types.VARCHAR);
			else {
				temp = "[";
				for (i=0;i<currentIndividual.ForeNames.size();i++) {
					temp += String.format("\"%s\",", escapeQuotes((String)currentIndividual.ForeNames.get(i)));
				}
				temp = temp.substring(0,temp.length()-1) + "]";
				preparedStatement.setString(4,temp);
			}
			
	    	if (currentIndividual.birthDate == null)
	    		preparedStatement.setNull(5, Types.DATE);
	    	else
	    		preparedStatement.setString(5, gedcomToSqlDate(currentIndividual.birthDate));
	    	
	    	preparedStatement.setString(6,currentIndividual.birthPlace);
	    	
	    	if (currentIndividual.deathDate == null)
	    		preparedStatement.setNull(7, Types.DATE);
	    	else
	    		preparedStatement.setString(7, gedcomToSqlDate(currentIndividual.deathDate));
	    	
	    	preparedStatement.setString(8,currentIndividual.deathPlace);
			
	    	preparedStatement.setInt(9,currentIndividual.parentFamily);
			
	    	if (currentIndividual.ownFamilies.size()==0)
				preparedStatement.setNull(10, Types.INTEGER);
			else {
				temp = "[";
				for (i=0; i<currentIndividual.ownFamilies.size();i++) {
					temp += currentIndividual.ownFamilies.get(i) + ",";
					}
				temp = temp.substring(0,temp.length()-1) + "]";
				preparedStatement.setString(10,temp) ;	
			}
			
	    	preparedStatement.setString(11,currentIndividual.gender.toString());
			
	    	preparedStatement.setString(12,currentIndividual.comment);
            
	    	preparedStatement.executeUpdate();					    		
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
