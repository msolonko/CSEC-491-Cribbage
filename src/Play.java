import cpsc474.*;

import java.io.IOException;
import java.util.*;
/**
 * The `Play` class allows the user to play against a Cribbage Agent of choice. It provides a menu for selecting the throwing and pegging agents, and allows the user to specify the number of games to play.
 * The class contains a `main` method which reads user input and executes the game, and a `play` method which runs a single game of Cribbage.
 */
public class Play {
    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Welcome! Here, you can play against a Cribbage Agent of choice. For thrower, suited refers to throwing policies that pay attention to suit, while unsuited means those that do not. In the context of pegging policies, complete means those that look at current hand, sequence, and cards played by both players previously, while incomplete looks only at hand and current sequence. ");
        System.out.println("What throwing agent do you want to play? Enter greedy, random, schell, suited, or unsuited: ");
        String throwType = scanner.nextLine();

        CribbageGame game = new CribbageGame();
        KeepPolicy thrower = new GreedyThrower(game);
        NodeLoader nodeLoader = new NodeLoader();
        if (throwType.equals("random")) {
            thrower = new RandomThrower();
            System.out.println("You selected cpsc474.RandomThrower");
        }
        else if (throwType.equals("schell")) {
            thrower = new SchellThrower(game);
            System.out.println("You selected SchellThrower");
        }
        else if (!throwType.equals("greedy")) {
            System.out.println("You selected a CFR Thrower");
            System.out.println("Enter the filename for the nodes for this CFR throwing agent: ");
            String filename = scanner.nextLine();
            thrower = new CFRThrower(game, thrower, nodeLoader.getThrowNodes(filename), throwType.equals("suited"));
        }
        else {
            System.out.println("You selected cpsc474.GreedyThrower");
        }

        System.out.println("What pegging agent do you want to play? Enter greedy, random, rule, montecarlo, complete or incomplete");
        String pegType = scanner.nextLine();
        PegPolicy pegger = new GreedyPegger();
        if (pegType.equals("random")) {
            pegger = new RandomPegger();
            System.out.println("You selected cpsc474.RandomPegger");
        }
        else if(pegType.equals("rule")) {
            pegger = new RulePegger(game);
            System.out.println("You selected RulePegger");
        }
        else if(pegType.equals("montecarlo")) {
            pegger = new MCPegging();
            System.out.println("You selected MonteCarlo Pegging.");
        }
        else if (!pegType.equals("greedy")) {
            System.out.println("You selected CFR Pegging.");
            System.out.println("Enter the filename for the nodes for this pegging agent: ");
            String filename = scanner.nextLine();
            pegger = new CFRPeggingPolicy(pegger, nodeLoader.getPegNodes(filename), pegType.equals("complete"));
        }
        else {
            System.out.println("You selected Greedy Pegging");
        }

        CompoundPolicy oppPolicy = new CompoundPolicy(thrower, pegger);

        System.out.println("Great! The opponent policy has been set.");

        System.out.println("# games do you want to play: ");
        int numGames = Integer.parseInt(scanner.nextLine());

        for (int i  = 0; i < numGames; i++) {
            play(oppPolicy, game, i%2, scanner);
        }

    }
    public static void play(CribbagePolicy oppPolicy, CribbageGame game, int playerNum, Scanner scanner)
    {
        int[] scores = new int[] {0, 0};
        int dealer = 0;


        while (MoreArrays.max(scores) < 121)
        {
            if (dealer == playerNum) {
                System.out.println("You are the dealer");
            }
            else {
                System.out.println("The opponent is the dealer");
            }
            // deal cards
            CribbageHand[] cardsInPlay = game.deal();

            // turned card is first (only) element of third part of deal
            CribbageCard turn = cardsInPlay[2].iterator().next();

            // check for 2 for heels (turned card is a Jack)
            int heels = game.turnCardValue(turn);
            scores[dealer] += heels;


            // get keep/throw for each player
            CribbageHand[][] keeps = new CribbageHand[2][];
            for (int p = 0; p < 2; p++)
            {
                if (p == playerNum) {
                    boolean correct = false;
                    while (!correct) {
                        System.out.println("These are your cards: " + cardsInPlay[p]);
                        System.out.println("Enter the first index of the cards you will throw: ");
                        int a = Integer.parseInt(scanner.nextLine());
                        System.out.println("Enter the second index of the cards you will throw: ");
                        int b = Integer.parseInt(scanner.nextLine());
                        if (b < a) {
                            int c = a;
                            a = b;
                            b = c;
                        }
                        keeps[p] = cardsInPlay[p].split(new ArrayList<>(Arrays.asList(a, b)));
                        correct = cardsInPlay[p].isLegalSplit(keeps[p]);
                        if (!correct) {
                            System.out.println("Invalid split. try again.");
                        }
                    }
                }
                else {
                    keeps[p] = oppPolicy.keep(cardsInPlay[p], scores, p == dealer);
                }

            }

            // initialize pegging
            int pegTurn = 1 - dealer;
            PeggingHistory history = new PeggingHistory(game);
            CribbageHand[] pegCards = new CribbageHand[] {keeps[0][0], keeps[1][0]};
            System.out.println("Turn card: " + turn);
            while (MoreArrays.max(scores) < 121
                    && !history.isTerminal())
            {
                // get player's played card
                CribbageCard play = null;
                if (pegTurn != playerNum) {
                    play = oppPolicy.peg(pegCards[pegTurn], history, pegTurn == 0 ? Arrays.copyOf(scores, scores.length) : MoreArrays.reverse(scores), pegTurn == dealer);
                    System.out.println("Opponent plays: " +play);
                }
                else {
                    boolean valid = false;
                    while (!valid) {
                        System.out.println("Current sequence: " + getCurSeq(history) + ". Sum: " + history.getTotal());

                        boolean canPlay = false;
                        List<CribbageCard> validActions = new ArrayList<>();
                        for (CribbageCard c : pegCards[pegTurn]) {
                            if (history.isLegal(c, dealer == pegTurn ? 0 : 1)){
                                canPlay = true;
                            }
                        }

                        if (!canPlay) {
                            System.out.println("This turn, you have no legal play.");
                            valid = true;
                        } else {
                            System.out.println("This is your current hand: " + pegCards[pegTurn]);
                            System.out.println("Enter the index of the card you want to play: ");
                            int ind = Integer.parseInt(scanner.nextLine());
                            play = pegCards[pegTurn].cards.get(ind);
                            if (!history.isLegal(play, pegTurn == dealer ? 0 : 1)) {
                                System.out.println("Not a valid index. Try again.");
                            }
                            else {
                                valid = true;
                            }
                        }
                    }
                }


                // check for legality of chosen card
                if (play == null && history.hasLegalPlay(pegCards[pegTurn], pegTurn == dealer ? 0 : 1))
                {
                    throw new RuntimeException("passing when " + pegCards[pegTurn] + " contains a valid card");
                }
                else if (play != null && !history.isLegal(play, pegTurn == dealer ? 0 : 1))
                {
                    throw new RuntimeException("chosen card " + play + " us not legal");
                }

                history = history.play(play, pegTurn == dealer ? 0 : 1);

                // score the play
                int[] playScore = history.getScore();
                if (playScore[0] > 0)
                {
                    scores[pegTurn] += playScore[0];
                }
                else if (playScore[0] < 0)
                {
                    scores[1 - pegTurn] += -playScore[0];
                }

                // remove played card from hand
                if (play != null)
                {
                    CribbageHand newHand = pegCards[pegTurn].remove(play);
                    if (newHand == null)
                    {
                        throw new RuntimeException("played card " + play + " not in hand " + pegCards[pegTurn]);
                    }
                    pegCards[pegTurn] = newHand;
                }

                // next player's turn
                pegTurn = 1 - pegTurn;
            }

            // score non-dealer's hand
            if (MoreArrays.max(scores) < 121)
            {
                int[] handScore = game.score(keeps[1 - dealer][0], turn, false);
                scores[1 - dealer] += handScore[0];
                System.out.println("Non dealer hand score: " + handScore[0]);
            }

            // score dealer's hand
            if (MoreArrays.max(scores) < 121)
            {
                int[] handScore = game.score(keeps[dealer][0], turn, false);
                scores[dealer] += handScore[0];
                System.out.println("Dealer hand score: " + handScore[0]);
            }

            // score crib
            if (MoreArrays.max(scores) < 121)
            {
                CribbageHand crib = new CribbageHand(keeps[0][1], keeps[1][1]);
                int[] handScore = game.score(crib, turn, true);
                scores[dealer] += handScore[0];
                System.out.println("Crib score: " + handScore[0]);
            }

            System.out.println("Game scores: " + Arrays.toString(scores));

            // change dealer
            dealer = 1 - dealer;
        }
        System.out.println("Game scores: " + Arrays.toString(scores));
        System.out.println("Game value: " + game.gameValue(scores));

    }


    /**
     * Get list of cards played in the current round
     * @param history
     * @return list of cards
     */
    public static List<CribbageCard> getCurSeq(PeggingHistory history) {
        List<CribbageCard> cards = new ArrayList<>();

        if (history.startRound()) {
            return cards;
        }

        while (history != null) {
            CribbageCard card = history.card;
            if (card != null) {
                cards.add(card);
            }
            history = history.prevPlay;
        }
        Collections.reverse(cards);
        return cards;
    }
}
