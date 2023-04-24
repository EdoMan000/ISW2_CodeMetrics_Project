package metrics.models;

public class Metrics {
    private boolean bugged;
    private int size;
    private int maxChurningFactor;
    private double avgChurningFactor;
    private int churn;
    private int maxAddedLOC;
    private double avgAddedLOC;
    private int addedLOC;

    public Metrics() {
        bugged = false;
        size = 0;
    }

    public boolean getBuggyness() {
        return bugged;
    }

    public void setBuggyness(boolean bugged) {
        this.bugged = bugged;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getSize() {
        return size;
    }

    public void setAddedLOC(int addedLOC) {
        this.addedLOC = addedLOC;
    }

    public int getAddedLOC() {
        return addedLOC;
    }

    public void setMaxAddedLOC(int maxAddedLOC) {
        this.maxAddedLOC = maxAddedLOC;
    }

    public int getMaxAddedLOC() {
        return maxAddedLOC;
    }

    public void setAvgAddedLOC(double avgAddedLOC) {
        this.avgAddedLOC = avgAddedLOC;
    }

    public double getAvgAddedLOC() {
        return avgAddedLOC;
    }

    public void setChurn(int churn) {
        this.churn = churn;
    }

    public int getChurn() {
        return churn;
    }

    public void setMaxChurningFactor(int maxChurningFactor) {
        this.maxChurningFactor = maxChurningFactor;
    }

    public int getMaxChurningFactor() {
        return maxChurningFactor;
    }

    public void setAvgChurningFactor(double avgChurningFactor) {
        this.avgChurningFactor = avgChurningFactor;
    }

    public double getAvgChurningFactor() {
        return avgChurningFactor;
    }
}
