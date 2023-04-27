import java.io.IOException;
import java.util.*;
/**
 The GetPegCard class is a program that calculates the best card to play in the game of Cribbage, given a certain game state.
 The program takes input from the command line in the form of a game state string, which includes information about the player's hand, the current sequence of cards played, and the total score so far. The program uses a hash table of pre-computed game states to quickly look up the best move for the given state, and outputs the corresponding card to play. If the state is not found in the hash table, the program calculates the best move by iterating through the player's hand and selecting the highest-valued card that can be played without exceeding a total score of 31.
 The program includes a method for calculating the value of a card given its rank, and a main method that reads input from the command line, calls the appropriate methods, and outputs the chosen card.
 This was made only to play Bill on cardgames.io. This performs worse than pegv2 does normally, since for the last 2 cards, Greedy is not being used as backup.
 The reason for that is because it was too complicated to reconstruct the entire cpsc474.PeggingHistory from playing the game on cardgames.io using Python and then push that here.
 So the simplification is, if the node does not exist in CFR, play the highest value legal card, which is definitely worse than Greedy.
 */
public class GetPegCard {
    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        NodeLoader nl = new NodeLoader();
        HashMap<String, PegNode> nodes = nl.getPegNodes("pegnodes_v2_3000k.txt");
        while (true) {
            String input = scanner.nextLine();
            if (input.equals("quit")) break;

            String hand = input.split("\\|", -1)[0];

            if (nodes.containsKey(input)) {
                int a = nodes.get(input).getAction();
                String chosenCard = hand.substring(a, a+1);
                System.out.println(chosenCard);
            }
            else {
                String curSeq = input.split("\\|", -1)[3];
                int total = 0;
                for (int i = 0; i < curSeq.length(); i++) {
                    char c = curSeq.charAt(i);
                    total += getCardVal(c);
                }

                String chosenCard = "A";
                for (int i = 0; i < hand.length(); i++) {
                    String card = hand.substring(i, i+1);
                    char c = card.charAt(0);
                    if (total + getCardVal(c) <= 31 && getCardVal(c) > getCardVal(chosenCard.charAt(0))) {
                        chosenCard = card;
                    }
                }
                System.out.println(chosenCard);

            }

        }

    }
    public static int getCardVal(char c) {
        if (c == 'A') return 1;
        else if (c == 'J' || c == 'K' || c == 'Q' || c == 'T') {
            return 10;
        }
        return Integer.parseInt(String.valueOf(c));
    }

}
