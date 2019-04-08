/*
 *  This class corresponds to the Individual table is the database
 */
import java.util.ArrayList;
import java.util.List;

public class Individual {
	public int id = 0;  // primary key in table
	public String BirthFamilyName;
	public List OtherFamilyNames = new ArrayList();
	public List ForeNames= new ArrayList();
	public String birthDate;
	public String birthPlace;
	public String deathDate;
	public String deathPlace;
	public int parentFamily;
	public List ownFamilies = new ArrayList();
	public enum eGender {Male,Female,Other};
	public eGender gender;
	public String comment; 
	public enum eDP {UNKNOWN,BIRTH,DEATH};
	public eDP addingTo = eDP.UNKNOWN;  // which field DATE/PLAC refers to
	public void clear()
	{
		id = 0;
		BirthFamilyName = birthDate = birthPlace = deathDate = deathPlace = comment = null;
		parentFamily = 0;
		OtherFamilyNames.clear();
		ForeNames.clear();
		ownFamilies.clear();
		gender = eGender.Other;
		addingTo = eDP.UNKNOWN;
	}
}
