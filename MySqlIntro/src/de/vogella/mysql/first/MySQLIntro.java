package de.vogella.mysql.first;
import de.vogella.mysql.first.MySQLAccess;

public class MySQLIntro {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		MySQLAccess dao = new MySQLAccess();
        try {
			dao.readDataBase();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
