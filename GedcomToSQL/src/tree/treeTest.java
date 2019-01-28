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
		int [] children = getdbChildren(1);
		for (int c: children)
			root.addChild(new Node<String>("node " + c));
		//System.out.println(children);
		//Node<String> root = createTree();
		printTree(root, "-");
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
	
	private static int [] getdbChildren(int start) {
		int [] children;
		int family;
		String myfamily = "";
		String myChildren = "";
		ResultSet rs;
		String sql = "Select ownfamily from Individual where id="+start+";";
 		try {
 			rs = stmt.executeQuery(sql);
 			while (rs.next())
 		    {
 				myfamily = rs.getString("OwnFamily");
 		    }
 		    stmt.close();
 		} catch (SQLException e) {
            System.out.println(e.getMessage());
        }
 		family = Integer.parseInt(myfamily);
		sql = "Select children from Family where id="+family+";";
 		try {
 			rs = stmt.executeQuery(sql);
 			while (rs.next())
 		    {
 				myChildren = rs.getString("children");
 		    }
 		    stmt.close();
 		} catch (SQLException e) {
            System.out.println(e.getMessage());
        }
 		String [] allChildren = myChildren.split(",");
 		children = new int[allChildren.length];
 		for (int i=0; i< allChildren.length;i++)
 			children[i] = Integer.parseInt(allChildren[i]);
		return children;
	}
}
