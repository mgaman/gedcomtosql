import java.util.ArrayList;
import java.util.List;

public class TreeWalk {

	public static void main(String[] args) {
		genericSQLhandler mh = new genericSQLhandler("10.0.0.3", "waterman", "qvwbenrm", "Family");
		System.out.println("DB Opened");
		GetFamilyData gg = new GetFamilyData(mh);
//		gg.debug = true;
		Individual indiv;
		Family family;
		long head = 11;
		// display the top of the tree
		indiv = gg.getIndividual(head);
		if (gg.debug) {
			System.out.println(String.format("Individual ID %d %s %s %s",
					indiv.id,indiv.ForeNames.get(0),indiv.BirthFamilyName,indiv.gender));
			family = gg.getFamily(1);
			System.out.println(String.format("Family ID %d father %d mother %d",
					family.ref,family.father,family.mother));	
		}
		List<treeEntry> allKids = gg.getDescendants(head,0);
		
		try {
			if (allKids.size()>0)
				for (int i=0;i<allKids.size();i++) {
//					System.out.println(String.format("ID: %d Parent: %d Level %d",
//							allKids.get(i).ID,allKids.get(i).parent,allKids.get(i).level));
					indiv = gg.getIndividual(allKids.get(i).ID);
					for (int j=0;j<allKids.get(i).level;j++)
						System.out.print("-");
					System.out.println(String.format("%s %s %s",
							indiv.ForeNames.get(0),indiv.BirthFamilyName,indiv.gender));
				}
			mh.disconnectMySQL();
			System.out.println("DB Closed");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
