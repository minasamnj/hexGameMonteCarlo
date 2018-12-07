package monteCarlo;

import java.util.Scanner;

public class HexApp {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int size = 10;

		HexBoard hb = new HexBoard(size);
		hb.generate_connected_graph();
		int computer_color = Graph.COLOR_RED, computer_start_node = HexBoard.SIDE_TOP(size), computer_end_node = HexBoard.SIDE_BOTTOM(size);
		int player_color = Graph.COLOR_BLUE ,player_start_node = HexBoard.SIDE_LEFT(size), player_end_node = HexBoard.SIDE_RIGHT(size);
		hb.print_board();

		while(true) {
			System.out.println("Enter your move: ");
			// Using Scanner for Getting Input from User 
	        Scanner in = new Scanner(System.in); 
	        System.out.println("Enter X first: ");
			int x =-1, y = -1;
			if (in.hasNextInt())
				x= in.nextInt(); 
			System.out.println("Enter Y : ");
			if(in.hasNextInt())
				y= in.nextInt(); 
			
			System.out.println("X and Y: " + x +" "+ y);

			/* continue if it's an invalid move */
			if(!hb.make_move(hb.convert_coords(x, y), player_color)) {
				System.out.println("invalid move!");
				continue;
			}
			if (player_color == hb.board.bfs(player_start_node, player_end_node, player_color) )
			{
				System.out.println("you won!!" );
				break;
			}
			/* computer's turn */
			int ret = hb.computer_move(computer_start_node, computer_end_node ,computer_color);
			hb.print_board();
			/* the computer's move function calculates the end of the game for us */
			if(ret == Graph.COLOR_RED)
			{
				System.out.println("computer won!!" );
				break;
			}

		}

	}

}
