import cpsc474.*;

import java.io.IOException;
import java.util.Scanner;

public class Evaluation {
    static CribbageGame game = new CribbageGame();
    public static void main(String[] args) throws IOException {
        NodeLoader nodeLoader = new NodeLoader();
        Scanner scanner = new Scanner(System.in);
        System.out.println("Welcome! Here, you can evaluate different agents against one another. For thrower, suited refers to throwing policies that pay attention to suit, while unsuited means those that do not. In the context of pegging policies, complete means those that look at current hand, sequence, and cards played by both players previously, while incomplete looks only at hand and current sequence.");
        KeepPolicy thrower1 = getThrower(1, nodeLoader, scanner);
        PegPolicy pegger1 = getPegger(1, nodeLoader, scanner);
        System.out.println("You have finished setting up the first agent.");
        KeepPolicy thrower2 = getThrower(2, nodeLoader, scanner);
        PegPolicy pegger2 = getPegger(2, nodeLoader, scanner);
        System.out.println("You have finished setting up the second agent.");

        CompoundPolicy agent1 = new CompoundPolicy(thrower1, pegger1);
        CompoundPolicy agent2 = new CompoundPolicy(thrower2, pegger2);

        System.out.println("How many games to simulate: ");
        EvaluationResults ev = game.evaluatePolicies(agent1, agent2, Integer.parseInt(scanner.nextLine()));
        System.out.println(ev);
    }

    static KeepPolicy getThrower(int n, NodeLoader nodeLoader, Scanner scanner) throws IOException {
        System.out.println("What throwing policy do you want for agent " + n + "? Enter greedy, random, schell, cardgames, suited, or unsuited: ");
        String throwType = scanner.nextLine();
        CribbageGame game = new CribbageGame();
        KeepPolicy thrower = new GreedyThrower(game);
        if (throwType.equals("random")) {
            thrower = new RandomThrower();
            System.out.println("You selected cpsc474.RandomThrower");
        }
        else if (throwType.equals("cardgames")) {
            thrower = new CardGamesThrower(game);
            System.out.println("You selected BILL from cardgames.io");
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
        return thrower;
    }

    static PegPolicy getPegger(int n, NodeLoader nodeLoader, Scanner scanner) throws IOException {
        System.out.println("What pegging agent do you want to play? Enter greedy, cardgames, random, rule, montecarlo, complete or incomplete");
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
        else if (pegType.equals("cardgames")) {
            pegger = new CardGamesPegger();
            System.out.println("You selected BILL from cardgames.io");
        }
        else if(pegType.equals("montecarlo")) {
            pegger = new MCPegging();
            System.out.println("You selected MonteCarlo Pegging. Make sure to keep the number of simulations low, as this one is very slow. 10k iterations will take up to 10 minutes.");
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
        return pegger;
    }
}
