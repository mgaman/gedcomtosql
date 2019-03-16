import java.util.ArrayList;
import java.util.List;

class familyEntry {
	public int ID;  // into Individuals table
	public int level; // top of tree is level 0
	public int parent; 
}

public class SortTest {

	public static void main(String[] args) {
		List<familyEntry> nodes = new ArrayList<familyEntry>();
		familyEntry te = new familyEntry();
		te.ID = 1;
		te.level = 0;
		te.parent = 0;
		nodes.add(te);
		te.ID = 3;
		te.level = 1;
		te.parent = 1;
		nodes.add(te);
		te.ID = 5;
		nodes.add(te);
		te.ID = 879;
		te.level = 2;
		te.parent = 3;
		nodes.add(te);
		te.ID = 1119;
		nodes.add(te);
		te.ID = 1122;
		te.parent = 5;
		nodes.add(te);
		te.ID = 1165;
		nodes.add(te);
		te.ID = 1475;
		nodes.add(te);
		for (int i=0; i < nodes.size(); i++)
			System.out.println(nodes.get(i).ID+","+nodes.get(i).level+","+nodes.get(i).parent);
	}

}
