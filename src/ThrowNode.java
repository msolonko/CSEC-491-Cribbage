public class ThrowNode extends CFRNode{
    // 0/1 representing whether dealer + sorted string of cards by value excluding
    int[][] combs = {{0, 1}, {0, 2}, {0, 3}, {0, 4}, {0, 5}, {1, 2}, {1, 3}, {1, 4}, {1, 5}, {2, 3}, {2, 4}, {2, 5}, {3, 4}, {3, 5}, {4, 5}};

    private boolean sample = false;
    public ThrowNode() {
        super((byte)15);
    }

    public ThrowNode(boolean sample) {
        super((byte)15);
        this.sample = sample;
    }

    public int[] getAction() {
        if (this.sample) return combs[sampleBestAction()];
        return combs[getBestAction()];
    }

    /*@Override
    public int getBestAction() {
    // second best action - performs MUCH worse
        float[] avgStrat = getAverageStrategy();
        float[] clonedArr = avgStrat.clone();
        Arrays.sort(clonedArr);
        float secondMax = clonedArr[clonedArr.length-2];
        for (int i = 0; i < avgStrat.length; i++) {
            if (avgStrat[i] == secondMax) {
                return i;
            }
        }
        return 0;

    }*/
}

