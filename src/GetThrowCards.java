import cpsc474.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
/**
 The GetThrowCards class is a program that calculates the best cards to throw in the game of Cribbage, given a certain hand and game state.
 The program takes input from the command line in the form of a game state string and uses the input to construct a cpsc474.CribbageHand object. The program then uses the Counterfactual Regret Minimization (CFR) algorithm to calculate the best two cards to throw from the hand, and outputs the corresponding cards to the console.
 The program includes a method for creating a list of cpsc474.CribbageCard objects from the input string, and a main method that reads input from the command line, constructs the appropriate objects, calls the CFR algorithm to determine the best cards to throw, and outputs the result.
 The program relies on external data files containing pre-computed game states and strategies (specifically, the suit-blind thrownodes_v2 policy), which are loaded using a NodeLoader object.
 */
public class GetThrowCards {
    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        NodeLoader nl = new NodeLoader();
        CribbageGame game = new CribbageGame();
        CFRThrower thrower = new CFRThrower(game, new GreedyThrower(game), nl.getThrowNodes("thrownodes_v2_1000k.txt"), false);
        while (true) {
            String input = scanner.nextLine();
            if (input.equals("quit")) break;
            boolean dealer = input.substring(0, 1).equals("1");
            CribbageHand hand = new CribbageHand(createCribbageCardList(input.substring(1)));
            List<CribbageCard> cards = thrower.keep(hand, null, dealer)[1].cards;
            System.out.println(cards.get(0) + "|" + cards.get(1));
        }

    }
    public static List<CribbageCard> createCribbageCardList(String input) {
        List<CribbageCard> cards = new ArrayList<>();

        HashMap<String, Integer> rankToVal = new HashMap<>();
        // construct mapping rank to value
        rankToVal.put("A", 0);
        for (int i = 2; i <= 9; i++) {
            rankToVal.put(Integer.toString(i), i-1);
        }
        // not the point values - just mapping to make sure we can sort correctly and reproducible
        rankToVal.put("T", 9);
        rankToVal.put("J", 10);
        rankToVal.put("Q", 11);
        rankToVal.put("K", 12);

        for (int i = 0; i < input.length(); i += 2) {
            String rank = input.substring(i, i + 1);
            String suit = input.substring(i + 1, i + 2);
            cards.add(new CribbageCard(new CardRank(rankToVal.get(rank), rank), suit.charAt(0)));
        }

        return cards;
    }

}
