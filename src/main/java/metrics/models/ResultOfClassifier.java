package metrics.models;

public class ResultOfClassifier {
    private final String projName;
    private final int walkForwardIteration;
    private final String classifierName;
    private final boolean hasFeatureSelection;
    private final boolean hasSampling;
    private double trainingPercent;
    private double precision;
    private double recall;
    private double areaUnderROC;
    private double kappa;
    private double truePositives;
    private double falsePositives;
    private double trueNegatives;
    private double falseNegatives;

    public ResultOfClassifier(String projName, int walkForwardIteration, String classifierName, boolean hasFeatureSelection, boolean hasSampling) {
        this.projName = projName;
        this.walkForwardIteration = walkForwardIteration;
        this.classifierName = classifierName;
        this.hasFeatureSelection = hasFeatureSelection;
        this.hasSampling = hasSampling;

        trainingPercent = 0.0;
        precision = 0;
        recall = 0;
        areaUnderROC = 0;
        kappa = 0;
        truePositives = 0;
        falsePositives = 0;
        trueNegatives = 0;
        falseNegatives = 0;
    }

    public void setTrainingPercent(double trainingPercent) {
        this.trainingPercent = trainingPercent;
    }

    public double getTrainingPercent() {
        return trainingPercent;
    }

    public void setPrecision(double precision) {
        this.precision = precision;
    }

    public double getPrecision() {
        return precision;
    }

    public void setRecall(double recall) {
        this.recall = recall;
    }

    public double getRecall() {
        return recall;
    }

    public void setAreaUnderROC(double areaUnderROC) {
        this.areaUnderROC = areaUnderROC;
    }

    public double getAreaUnderROC() {
        return areaUnderROC;
    }

    public void setKappa(double kappa) {
        this.kappa = kappa;
    }

    public double getKappa() {
        return kappa;
    }

    public void setTruePositives(double truePositives) {
        this.truePositives = truePositives;
    }

    public double getTruePositives() {
        return truePositives;
    }

    public void setFalsePositives(double falsePositives) {
        this.falsePositives = falsePositives;
    }

    public double getFalsePositives() {
        return falsePositives;
    }

    public void setTrueNegatives(double trueNegatives) {
        this.trueNegatives = trueNegatives;
    }

    public double getTrueNegatives() {
        return trueNegatives;
    }

    public void setFalseNegatives(double falseNegatives) {
        this.falseNegatives = falseNegatives;
    }

    public double getFalseNegatives() {
        return falseNegatives;
    }

    public String getProjName() {
        return projName;
    }

    public int getWalkForwardIteration() {
        return walkForwardIteration;
    }

    public String getClassifierName() {
        return classifierName;
    }

    public boolean hasFeatureSelection() {
        return hasFeatureSelection;
    }

    public boolean hasSampling() {
        return hasSampling;
    }
}
