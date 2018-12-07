package monteCarlo;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue; 

public class Graph implements Cloneable{

	public static final int COLOR_RED = 1;
	public static final int COLOR_BLUE = 2;

	/* 2-D matrix of path lengths */
	public int[][] conn;

	public int[] node_color;
	public int[] node_data;
	public int num;

	public Object clone() throws
	CloneNotSupportedException 
	{ 
		// Assign the shallow copy to new refernce variable t 
		Graph t = (Graph)super.clone(); 

		t.conn = this.conn.clone();
		t.node_color = this.node_color.clone();
		t.node_data = this.node_data.clone();

		return t; 
	} 
	public Graph(int n) {
		num = n;
		conn   =  new int[n][n];
		node_color = new int[n];
		node_data= new int[n];
		/* nothing connected */
		for(int i=0;i<n;i++) {
			node_color[i] = 0;
			node_data[i] = -1;
			for(int j=0;j<n;j++) {
				conn[i][j] = -1;
				conn[j][i] = -1;
			}
		}
	}


	public int getNum() {
		return num;
	}

	public void setNum(int num) {
		this.num = num;
	}

	public int bfs(int start, int end, int color)
	{
		if(node_color[start] != node_color[end] && node_color[end] != color) return 0;

		// Create a queue for BFS 
		LinkedList<Integer> queue = new LinkedList<Integer>();
		boolean[] visited = new boolean[num];
		visited[start]=true; 
		queue.add(start); 
		while(queue.size() != 0) {
			int t = queue.poll(); 
			if(t == end)
				return color;
			for(int i=0;i<num;i++)
			{
				if(conn[i][t] != -1 && node_color[i] == color && !visited[i]) {
					visited[i] = true;
					queue.add(i); 
				}
			}
		}
		return 0;
	}

	/* register these two nodes as connected. Path length doesn't matter, that's calculated via 
	 * the colors of the nodes */
	public void connect_nodes(int a, int b)
	{
		conn[a][b] = 1;
		conn[b][a] = 1;
	}

	public void set_node_color(int node, int color) {
		node_color[node] = color;

	}

}
