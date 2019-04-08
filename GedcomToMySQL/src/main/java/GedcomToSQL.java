import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
//import java.sql.Connection;

public class GedcomToSQL {

	public static void main(String[] args) {
		genericSQLhandler mh = new genericSQLhandler("10.0.0.3", "waterman", "qvwbenrm", "Family");
		GedcomProcessor gedcom = new GedcomProcessor(mh);
		try {
//			Connection con = mh.connectMySQL();
			FileInputStream fstream = new FileInputStream("src/main/resources/transfer.ged");
			// Get the object of DataInputStream
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			while ((strLine = br.readLine()) != null) {
				// System.out.println(strLine);
				gedcom.processLine(strLine);
			}
			in.close();
			gedcom.printStats();
		} catch (Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
	}
}
