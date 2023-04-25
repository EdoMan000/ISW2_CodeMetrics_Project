package metrics.models;

public class LOCMetrics {
    private int maxVal;
    private double avgVal;
    private int val;

    public LOCMetrics() {
    }

    public int getMaxVal() {
        return maxVal;
    }

    public void setMaxVal(int maxVal) {
        this.maxVal = maxVal;
    }

    public double getAvgVal() {
        return avgVal;
    }

    public void setAvgVal(double avgVal) {
        this.avgVal = avgVal;
    }

    public int getVal() {
        return val;
    }

    public void setVal(int val) {
        this.val = val;
    }

    public void addToVal(int val) {
        this.val += val;
    }
}
