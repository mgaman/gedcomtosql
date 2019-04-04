import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.Connection;

public class GedcomToMSQL {

	public static void main(String[] args) {
		GedcomProcessor gedcom = new GedcomProcessor();
		mySQLhandler mh = new mySQLhandler("10.0.0.3", "waterman", "qvwbenrm", "WaterMeter");
		try {
			Connection con = mh.openDB();
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
