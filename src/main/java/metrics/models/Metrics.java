package metrics.models;

public class Metrics {
    private final LOCMetrics_ValAvgMax valAvgMaxRemovedLOC = new LOCMetrics_ValAvgMax();
    private final LOCMetrics_ValAvgMax valAvgMaxChurnLOC = new LOCMetrics_ValAvgMax();
    private final LOCMetrics_ValAvgMax valAvgMaxAddedLOC = new LOCMetrics_ValAvgMax();
    private boolean bugged;
    private int size;

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
        this.valAvgMaxAddedLOC.val = addedLOC;
    }

    public int getAddedLOC() {
        return valAvgMaxAddedLOC.val;
    }

    public void setMaxAddedLOC(int maxAddedLOC) {
        this.valAvgMaxAddedLOC.maxVal = maxAddedLOC;
    }

    public int getMaxAddedLOC() {
        return valAvgMaxAddedLOC.maxVal;
    }

    public void setAvgAddedLOC(double avgAddedLOC) {
        this.valAvgMaxAddedLOC.avgVal = avgAddedLOC;
    }

    public double getAvgAddedLOC() {
        return valAvgMaxAddedLOC.avgVal;
    }

    public void setChurn(int churn) {
        this.valAvgMaxChurnLOC.val = churn;
    }

    public int getChurn() {
        return valAvgMaxChurnLOC.val;
    }

    public void setMaxChurningFactor(int maxChurningFactor) {
        this.valAvgMaxChurnLOC.maxVal = maxChurningFactor;
    }

    public int getMaxChurningFactor() {
        return valAvgMaxChurnLOC.maxVal;
    }

    public void setAvgChurningFactor(double avgChurningFactor) {
        this.valAvgMaxChurnLOC.avgVal = avgChurningFactor;
    }

    public double getAvgChurningFactor() {
        return valAvgMaxChurnLOC.avgVal;
    }

    public void setRemovedLOC(int removedLOC) {
        this.valAvgMaxRemovedLOC.val = removedLOC;
    }

    public int getRemovedLOC() {
        return valAvgMaxRemovedLOC.val;
    }

    public void setMaxRemovedLOC(int maxRemovedLOC) {
        this.valAvgMaxRemovedLOC.maxVal = maxRemovedLOC;
    }

    public int getMaxRemovedLOC() {
        return valAvgMaxRemovedLOC.maxVal;
    }

    public void setAvgRemovedLOC(double avgRemovedLOC) {
        this.valAvgMaxRemovedLOC.avgVal = avgRemovedLOC;
    }

    public double getAvgRemovedLOC() {
        return valAvgMaxRemovedLOC.avgVal;
    }

    public static class LOCMetrics_ValAvgMax {
        private int maxVal;
        private double avgVal;
        private int val;

        public LOCMetrics_ValAvgMax() {
        }
    }
}
