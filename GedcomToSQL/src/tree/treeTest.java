package tree;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;


class Node <T> {
	private T data = null;
	private List<Node<T>> children = new ArrayList<>(); 
	private Node<T> parent = null;
	public Node(T data) {
		this.data = data;
	}
	public Node<T> addChild(Node<T> child) {
		child.setParent(this);
		this.children.add(child);
		return child;
	}
	public void addChildren(List<Node<T>> children) {
		children.forEach(each -> each.setParent(this));
		this.children.addAll(children);
	}
	public List<Node<T>> getChildren() {
		return children;
	}
	public T getData() {
		return data;
	}
	public void setData(T data) {
		this.data = data;
	}
	private void setParent(Node<T> parent) {
		this.parent = parent;
	}
	public Node<T> getParent() {
		return parent;
	}	
}

public class treeTest {
	static Connection conn = null;
	static Statement stmt = null;
	ResultSet rs = null;

	public static void main(String[] args) {
		// setup database
		try {
			Class.forName("org.sqlite.JDBC");
			conn = DriverManager.getConnection("jdbc:sqlite:src/Data/family.db");
			System.out.println("Opened database successfully");
			conn.setAutoCommit(true);
			stmt = conn.createStatement();
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}

		Node<String> root = new Node<>("root");
//		getDescendants(6,3);  // 1 family,many descendants
//		getDescendants(879,1);  // no family
//		getDescendants(30,1);  // 1 family, no kids
      getDescendants(8,0);  // 3 families, 1,0,3 kids
      //getAscendants(1,8);
//		for (int c: children)
//			root.addChild(new Node<String>("node " + c));
		//System.out.println(children);
		//Node<String> root = createTree();
		//printTree(root, "-");
	}
	private static Node<String> createTree() {
		Node<String> root = new Node<>("root");

		Node<String> node1 = root.addChild(new Node<String>("node 1"));

		Node<String> node11 = node1.addChild(new Node<String>("node 11"));
		Node<String> node111 = node11.addChild(new Node<String>("node 111"));
		Node<String> node112 = node11.addChild(new Node<String>("node 112"));

		Node<String> node12 = node1.addChild(new Node<String>("node 12"));

		Node<String> node2 = root.addChild(new Node<String>("node 2"));

		Node<String> node21 = node2.addChild(new Node<String>("node 21"));
		Node<String> node211 = node2.addChild(new Node<String>("node 22"));
		return root;
	}
	private static <T> void printTree(Node<T> node, String appender) {
		System.out.println(appender + node.getData());
		node.getChildren().forEach(each ->  printTree(each, appender + appender));
	}
	
	private static void getAscendants(int person,int maxdepth)
	{
		ResultSet Parentset,FatherMotherset;
		String sql = "Select ParentFamily from Individual where id="+person+";";
		int father = -1;
		int mother = -1;
		try {
			Statement Pstmt = conn.createStatement();
			Parentset = Pstmt.executeQuery(sql);
			// NOTE java.sql.resultset cannot tell you how many rows there are
			while (Parentset.next())
			{
				Statement HWstmt = conn.createStatement();
				String pfamily = Parentset.getString("ParentFamily");
				sql = "Select father,mother from family where id=" + pfamily + ";";
				FatherMotherset = HWstmt.executeQuery(sql);
				while (FatherMotherset.next())
				{
					// get 0 in place of NULL
					father = FatherMotherset.getInt("Father");
					mother = FatherMotherset.getInt("Mother");	
					System.out.println("Depth," + maxdepth + " Person," + person + " Father," + father + " Mother," + mother);
				}
				HWstmt.close();
			}
			Pstmt.close();
		}
		catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		if (--maxdepth == 0)
			return;
		if (father > 0)
			getAscendants(father,maxdepth);	
		if (mother > 0)
			getAscendants(mother,maxdepth);	
		return;
	}
	
	private static void getDescendants(int parent, int maxdepth)
	{
		List<Integer> totalChildren = new ArrayList<Integer>();
		ResultSet ownfamilyset,childrenset;
		String myfamilies = null;
		String myChildren = null;
		String sql = "Select ownfamily from Individual where id="+parent+";";
		// check if parent has any families
		try {
			Statement Fstmt = conn.createStatement();
			ownfamilyset = Fstmt.executeQuery(sql);
			// NOTE java.sql.resultset cannot tell you how many rows there are
			while (ownfamilyset.next())
			{
				myfamilies = ownfamilyset.getString("OwnFamily");
				// myfamilies is a CSV of family(families)
				if (myfamilies != null)
				{
					String [] Families = myfamilies.split(",");
					// loop over all families
					for (int c=0; c<Families.length;c++)
					{
						sql = "Select children from Family where id="+Families[c]+";";
						try {
							Statement Cstmt = conn.createStatement();
							childrenset = Cstmt.executeQuery(sql);
							while (childrenset.next())
							{
								myChildren = childrenset.getString("children");
								// myChildren is a CSV of children
								if (myChildren != null)
								{
									String [] allChildren = myChildren.split(",");
									for (int i=0; i< allChildren.length;i++)
									{
										totalChildren.add(Integer.parseInt(allChildren[i]));
										System.out.println(maxdepth + "," + parent + "," + Integer.parseInt(allChildren[i]));
									}
								}
							}
							Cstmt.close();
						} catch (SQLException e) {
							System.out.println(e.getMessage());
						}
					}
				}
			}
			Fstmt.close();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	//	if (--maxdepth == 0)
	//		return;
		++maxdepth;
		while (!totalChildren.isEmpty())
			getDescendants((int)totalChildren.remove(0),maxdepth);	
		return;
	}	
}
