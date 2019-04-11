import java.util.ArrayList;
import java.util.List;

public class TreeWalk {

	public static void main(String[] args) {
		genericSQLhandler mh = new genericSQLhandler("10.0.0.3", "waterman", "qvwbenrm", "Family");
		System.out.println("DB Opened");
		GetFamilyData gg = new GetFamilyData(mh);
//		gg.debug = true;
		Individual indiv;
		List<treeEntry> allKids = gg.getDescendants(11,0);
		try {
			if (allKids.size()>0)
				for (int i=0;i<allKids.size();i++) {
//					System.out.println(String.format("ID: %d Parent: %d Level %d",
//							allKids.get(i).ID,allKids.get(i).parent,allKids.get(i).level));
					indiv = gg.getIndividual(allKids.get(i).ID);
					for (int j=0;j<allKids.get(i).level;j++)
						System.out.print("-");
					System.out.println(String.format("%s %s",
							indiv.ForeNames.get(0),indiv.BirthFamilyName));
				}
			mh.disconnectMySQL();
			System.out.println("DB Closed");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
