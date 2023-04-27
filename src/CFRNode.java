import java.util.Arrays;
import java.util.Random;

/**
 The CFRNode class represents a node in the Counterfactual Regret Minimization (CFR) algorithm used for solving imperfect-information games.
 This class stores the regret sum, strategy, and strategy sum for each action in the node, as well as the number of actions available.
 The class provides methods for calculating the strategy and average strategy for the node, selecting the best action, and sampling an action based on the average strategy probabilities.
 */
public class CFRNode {
    float[] regretSum;
    float[] strategy;
    float[] strategySum;

    byte numActions;

    public CFRNode(byte numActions) {
        this.numActions = numActions;
        regretSum = new float[numActions];
        Arrays.fill(regretSum, 0.0f);
        strategy = new float[numActions];
        Arrays.fill(strategy, 0.0f);
        strategySum = new float[numActions];
        Arrays.fill(strategySum, 0.0f);
    }

    public float[] getStrategy(double realizationWeight) {
        float normalizingSum = 0.0f;
        for (int i = 0; i < numActions; i++) {
            strategy[i] = Math.max(regretSum[i], 0);
            normalizingSum += strategy[i];
        }

        for (int i = 0; i < numActions; i++) {
            strategy[i] = (normalizingSum > 0) ? strategy[i] / normalizingSum : 1.0f / numActions;
            strategySum[i] += realizationWeight * strategy[i];
        }
        return strategy;
    }

    public float[] getAverageStrategy() {
        float[] averageStrategy = new float[numActions];
        Arrays.fill(averageStrategy, 0.0f);
        float normalizingSum = 0.0f;
        for (float s: strategySum) {
            normalizingSum += s;
        }
        for (int i = 0; i < numActions; i++) {
            averageStrategy[i] = (normalizingSum > 0) ? strategySum[i] / normalizingSum : 1.0f / numActions;

        }
        return averageStrategy;
    }

    /**
     * @return highest probability action index
     */
    public int getBestAction() {
        float[] avgStrat = getAverageStrategy();
        int index = 0;
        float m = 0.0f;
        for (int i = 0;i < numActions; i++){
            if (avgStrat[i]>m){
                m = avgStrat[i];
                index = i;
            }
        }
        return index;
    }

    /**
     * @return action index based on average strategy probabilities
     */
    public int sampleBestAction() {
        Random rand = new Random();
        double randNum = rand.nextDouble();
        float[] avgStrat = getAverageStrategy();
        int i = 0;
        float cumProb = 0.0f;
        while (i < numActions - 1) {
            cumProb += avgStrat[i];
            if (cumProb > randNum) {
                break;
            }
            i++;
        }
        return i;
    }

    public String toString(){
        return Arrays.toString(getAverageStrategy());
    }
}
