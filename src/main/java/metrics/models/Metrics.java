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

    public int getAddedLOC() {
        return valAvgMaxAddedLOC.getVal();
    }

    public int getMaxAddedLOC() {
        return valAvgMaxAddedLOC.getMaxVal();
    }

    public double getAvgAddedLOC() {
        return valAvgMaxAddedLOC.getAvgVal();
    }

    public int getChurn() {
        return valAvgMaxChurnLOC.getVal();
    }

    public int getMaxChurningFactor() {
        return valAvgMaxChurnLOC.getMaxVal();
    }

    public double getAvgChurningFactor() {
        return valAvgMaxChurnLOC.getAvgVal();
    }

    public int getRemovedLOC() {
        return valAvgMaxRemovedLOC.getVal();
    }

    public int getMaxRemovedLOC() {
        return valAvgMaxRemovedLOC.getMaxVal();
    }

    public double getAvgRemovedLOC() {
        return valAvgMaxRemovedLOC.getAvgVal();
    }


    public void setAddedLOCMetrics(int addedLOC, int maxAddedLOC, double avgAddedLOC) {
        this.valAvgMaxAddedLOC.setVal(addedLOC);
        this.valAvgMaxAddedLOC.setMaxVal(maxAddedLOC);
        this.valAvgMaxAddedLOC.setAvgVal(avgAddedLOC);
    }
    public void setRemovedLOCMetrics(int removedLOC, int maxRemovedLOC, double avgRemovedLOC) {
        this.valAvgMaxRemovedLOC.setVal(removedLOC);
        this.valAvgMaxRemovedLOC.setMaxVal(maxRemovedLOC);
        this.valAvgMaxRemovedLOC.setAvgVal(avgRemovedLOC);
    }
    public void setChurnMetrics(int churn, int maxChurningFactor, double avgChurningFactor) {
        this.valAvgMaxChurnLOC.setVal(churn);
        this.valAvgMaxChurnLOC.setMaxVal(maxChurningFactor);
        this.valAvgMaxChurnLOC.setAvgVal(avgChurningFactor);
    }
}
