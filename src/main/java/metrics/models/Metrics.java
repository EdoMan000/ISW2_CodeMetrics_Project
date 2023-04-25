package metrics.models;

public class Metrics {
    private final LOCMetrics removedLOCMetrics = new LOCMetrics();
    private final LOCMetrics ChurnMetrics = new LOCMetrics();
    private final LOCMetrics AddedLOCMetrics = new LOCMetrics();
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

    public LOCMetrics getRemovedLOCMetrics() {
        return removedLOCMetrics;
    }

    public LOCMetrics getChurnMetrics() {
        return ChurnMetrics;
    }

    public LOCMetrics getAddedLOCMetrics() {
        return AddedLOCMetrics;
    }

    public void setAddedLOCMetrics(int addedLOC, int maxAddedLOC, double avgAddedLOC) {
        this.AddedLOCMetrics.setVal(addedLOC);
        this.AddedLOCMetrics.setMaxVal(maxAddedLOC);
        this.AddedLOCMetrics.setAvgVal(avgAddedLOC);
    }

    public void setRemovedLOCMetrics(int removedLOC, int maxRemovedLOC, double avgRemovedLOC) {
        this.removedLOCMetrics.setVal(removedLOC);
        this.removedLOCMetrics.setMaxVal(maxRemovedLOC);
        this.removedLOCMetrics.setAvgVal(avgRemovedLOC);
    }

    public void setChurnMetrics(int churn, int maxChurningFactor, double avgChurningFactor) {
        this.ChurnMetrics.setVal(churn);
        this.ChurnMetrics.setMaxVal(maxChurningFactor);
        this.ChurnMetrics.setAvgVal(avgChurningFactor);
    }
}
