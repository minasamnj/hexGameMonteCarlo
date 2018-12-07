package monteCarlo;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class HexBoard implements Cloneable{

	public int size;
	public int empty;
	public Graph board;
	public int last;
	
	// change for difficulty.
	static int num_iterations = 2000;
	
	public Object clone() throws
	CloneNotSupportedException 
	{ 
		// Assign the shallow copy to new refernce variable t 
		HexBoard t = (HexBoard)super.clone(); 

		t.board = (Graph) this.board.clone();
		return t; 
	} 

	public HexBoard(int size){

		this.size = size;
		empty = size * size;
		board = new Graph(empty + 4);

	}

	/* if this node is uncolored, we may make a move there */
	public boolean is_valid_move(int i)
	{
		if(i > size*size || i < 0) return false;
		return board.node_color[i] == 0;
	}

	// helper functions
	public static int SIDE_TOP(int n) {
		return n*n;
	} 
	public static int SIDE_RIGHT(int n) {
		return n*n+1;
	}
	public static int SIDE_BOTTOM(int n) {
		return n*n+2;
	}
	public static int SIDE_LEFT(int n) {
		return n*n+3;
	}

	public void generate_connected_graph()
	{
		/* the sides of the board are nodes from size*size through size*size+3 (aka, the 4 nodes
		 * that are greater than 0-n*n).
		 */
		int n = size;
		/*
		 * 0 1 2 3
		 *  4 5 6 7
		 *   8 9 10 11
		 *     12 13 14 15
		 * a node will connect to nodes (N=node, RW=row width)
		 *    N-RW
		 *    N-RW+1
		 *    N-1 
		 *    N+1
		 *    N+RW-1
		 *    N+RW
		 */
		/* Connect each node with it's neighbors */
		for (int i=0;i<n*n;i++)
		{
			if(!((i+1) % n == 0) && !(i < n))
				board.connect_nodes(i, i-n + 1);
			if(!(i%n == 0) && !(i+n >= n*n)/*not on left edge and not bottom row*/)
				board.connect_nodes(i, i+n - 1);
			if(!(i < n)/*not top row*/)
				board.connect_nodes(i, i-n);
			else
				board.connect_nodes(i, SIDE_TOP(n));
			if(!((i+1) % n == 0)/*not on right edge */)
				board.connect_nodes(i, i+1);
			else
				board.connect_nodes(i, SIDE_RIGHT(n));
			if(!(i%n == 0)/*not on left edge */)
				board.connect_nodes(i, i-1);
			else
				board.connect_nodes(i, SIDE_LEFT(n));
			if(!(i+n >= n*n)/*not bottom row*/)
				board.connect_nodes(i, i+n);
			else
				board.connect_nodes(i, SIDE_BOTTOM(n));

		}
		/* 'fake' nodes that are the colored sides of the board */
		board.node_color[SIDE_BOTTOM(n)] = Graph.COLOR_RED;
		board.node_color[SIDE_TOP(n)] = Graph.COLOR_RED;
		board.node_color[SIDE_LEFT(n)] = Graph.COLOR_BLUE;
		board.node_color[SIDE_RIGHT(n)] = Graph.COLOR_BLUE;

	}

	public int convert_coords(int x, int y)
	{
		/* convert from cartesian to array index */
		return x + y*size;
	}

	public boolean make_move(int node, int color)
	{
		/* if it's a valid move, set the color of the node */
		if(!is_valid_move(node)) return false;
		board.set_node_color(node, color);
		board.node_data[node]=0;
		last = node;
		empty--;
		return true;
	}


	public void sim_make_move(int node, int color, int current)
	{
		/* if it's a valid move, set the color of the node. here we check if
		 * the "node data" has not been set by 'real' moves or by the current
		 * simulation. This optimizes it quite a bit, since we don't have to 
		 * clone a board for each iteration of the simulation */
		if( (board.node_data[node] == 0) || ( board.node_data[node] == current)) return;
		board.set_node_color(node, color);
		board.node_data[node]=0;
		empty--;
	}

	public int simulate(int sim_num, int color, int start, int end, int node )
	{
		List<Integer> potential_moves = new ArrayList<>();
		/* human's color */
		int other_player_color = (color == Graph.COLOR_BLUE ? Graph.COLOR_RED : Graph.COLOR_BLUE);

		/* a list of empty spaces on the board */
		for(int i=0;i<size*size;i++)
		{
			/* don't include the initial move */ 
			// node_data has all -1 init and 0 when insert
			if(board.node_data[i] != 0 && i != node)
				potential_moves.add(i);
		}
		/* make the initial move */
		sim_make_move(node, color, sim_num);
		while(potential_moves.size() > 0 && empty > 0)
		{
			Random rand = new Random();
			/* pick a random move */
			int m = rand.nextInt(potential_moves.size());
			node = potential_moves.get(m);
			potential_moves.remove(m);
			/* other player makes a move */
			sim_make_move(node, other_player_color, sim_num);
			/* record that this move was done by this iteration of the simulation */
			board.node_data[node] = sim_num;
			/* filled up yet? */
			if(empty == 0 || potential_moves.size() == 0) break;
			/* pick a random move for the computer */
			m = rand.nextInt(potential_moves.size());
			node = potential_moves.get(m);
			potential_moves.remove(m);
			/* make the computer's random move, and record that it was done by this iteration */
			sim_make_move(node, color, sim_num);
			board.node_data[node] = sim_num;
		}
		/* search if we've won or not */
		int result = board.bfs(start, end, color);
		return result;
	}


	public int montecarlo(int start, int end ,int color)
	{

		/* make a list of all the potential moves */
		List<Integer> potential_moves = new ArrayList<>();
		for(int i=0;i<size*size;i++)
		{
			if(is_valid_move(i))
				potential_moves.add(i);
		}

		/* go through all of them and find the one with the highest winning probability */
		int best_move=potential_moves.get(0); // get first empty as best move for now.
		double best_move_val=0;
		for(Integer entry : potential_moves)
		{
			int node = entry.intValue();
			int wins=0, losses=0;
			/* clone the board, and make a a backup of some data so that
			 * we can re-use this cloned board without making a new one
			 * for each simulation */
			HexBoard sim = null;
			try {
				sim = (HexBoard) this.clone();
				int old_empty = sim.empty;
				for(int i=1;i<=num_iterations;i++) {
					sim.empty = old_empty;
					int res = sim.simulate(i, color, start, end, node );
					if(res == color) wins++;
					else losses++;
				}
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}

			double p = ((double)wins*100 / ((double)wins+losses));
			/* better than before? */
			if(p > best_move_val) {
				best_move_val = p;
				best_move = node;
			}
		}
		/* actually make the move */
		make_move(best_move, color);
		// check if computer win
		int ret = board.bfs(start, end, color);
		if(ret != 0) return ret;
		return 0;
	}

	public int get_random_move()
	{
		Random rand = new Random();
		List<Integer> potential_moves = new ArrayList<>();
		for(int i=0;i<size*size;i++)
		{
			if(is_valid_move(i))
				potential_moves.add(i);
		}
		int x = rand.nextInt(potential_moves.size());
		int node = potential_moves.get(x);
		return node;
	}
	
	public int computer_move(int start, int end ,int color)
	{
		return montecarlo( start, end ,color);
	}
	
	public void print_board() {
		System.out.println("     ");
		//System.out.print(" ");
		for(int i=0;i<size;i++)
		{
			System.out.print( " "+ i);
		}
		System.out.println("<--X axis");

		for(int i=0;i<size;i++)
		{
			// identation
			System.out.print( i+ "");
			for(int j=0;j<i;j++) {
				System.out.print( "=");
			}
			for(int j=0;j<size;j++)
			{
				switch(board.node_color[(i * size)+j]) {
				case 0:
					System.out.print( " "+ ".");
					break;
				case Graph.COLOR_BLUE:
					System.out.print( " "+ "X");
					break;
				case Graph.COLOR_RED:
					System.out.print( " "+ "O");
					break;

				}

			}
			System.out.println(" " +i);

		}
		for(int i=0;i<size + 1;i++)
		{
			System.out.print( " ");
		}
		for(int i=0;i<size;i++)
		{
			System.out.print( " "+ i);
		}
		System.out.println("\n^");
		System.out.println("|");
		System.out.println("Y axis");


	}

}
