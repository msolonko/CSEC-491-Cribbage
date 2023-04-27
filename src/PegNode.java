public class PegNode extends CFRNode {
    private boolean sample = false;
    public PegNode(byte numActions) {
        super(numActions);
    }

    public PegNode(byte numActions, boolean sample) {
        super(numActions);
        this.sample = sample;
    }

    public int getAction() {
        if (this.sample) return sampleBestAction();
        return getBestAction();
    }
}
