import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class GedcomToMSQL {

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

	private static void emitIndividual()
	{
		
	}
	private static void emitFamily()
	{
		
	}
	private static void addToFam(String s) {
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
			currentFamily.relationship = Family.eRL.MARRIED;
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

	private static void addToIndividual(String line) {
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
			for (int i=1;i<fnames.length;i++)
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
			currentIndividual.gender = sex;		
		}
		else
			indErrors++;
	}
	/**
	 *  We do not know that a record is complete until the reception of a new record with top == true
	 *  So we just accumulate data until next record comes along, then write out it out.
	 * 
	 * @param line - the line of text from the GEDCOM file
	 * @param gedcomType - Gedcom level 0 tag type relevant to this line
	 * @param top - True if this line is a level 0 tag, else false 
	 */
	public static void processType(String line, eTypes gedcomType, boolean top)
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
/**
	 * First level of parsing for each line from the gedcom file. Looks for a level 0 tag
	 * @param line
	 */
	static void processLine(String line)
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

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			FileInputStream fstream = new FileInputStream("src/main/resources/transfer.ged");
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

}
