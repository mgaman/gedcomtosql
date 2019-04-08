import java.util.ArrayList;
import java.util.List;

/*
 *  This class corresponds to the Family table in the database
 */
public class Family {
	public int ref;   // primary key in table
	public int father;
	public int mother;
	public enum eRL {Married,Divorced,Other};
	public eRL relationship = eRL.Other;
	public List children = new ArrayList();
	public String marriageDate;
	public String marriagePlace;
	public String divorceDate;
	public String divorcePlace;	
	public String comment;
	public enum eDP {UNKNOWN,MARRIAGE,DIVORCE};
	public eDP addingTo = eDP.UNKNOWN;  // which field DATE/PLAC refers to
	public void clear() {
		ref = father = mother = 0;
		marriageDate = marriagePlace = divorceDate = divorcePlace = comment = null;
		children.clear();
		addingTo = eDP.UNKNOWN;
	}
}
