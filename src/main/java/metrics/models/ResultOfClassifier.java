package metrics.models;

import weka.classifiers.Evaluation;

public class ResultOfClassifier {
    private final int walkForwardIteration;
    private final String classifierName;
    private final boolean hasFeatureSelection;
    private final boolean hasSampling;
    private final CustomClassifier customClassifier;
    private double trainingPercent;
    private double precision;
    private double recall;
    private final double areaUnderROC;
    private final double kappa;
    private final double truePositives;
    private final double falsePositives;
    private final double trueNegatives;
    private final double falseNegatives;

    public ResultOfClassifier(int walkForwardIteration, CustomClassifier customClassifier, Evaluation evaluation) {
        this.walkForwardIteration = walkForwardIteration;
        this.customClassifier = customClassifier;
        this.classifierName = customClassifier.getClassifierName();
        this.hasFeatureSelection = (!customClassifier.getFeatureSelectionFilterName().equals("NoSelection"));
        this.hasSampling = (!customClassifier.getSamplingFilterName().equals("NoSampling"));

        trainingPercent = 0.0;
        precision = evaluation.precision(0);
        recall = evaluation.recall(0);
        areaUnderROC = evaluation.areaUnderROC(0);
        kappa = evaluation.kappa();
        truePositives = evaluation.numTruePositives(0);
        falsePositives = evaluation.numFalsePositives(0);
        trueNegatives = evaluation.numTrueNegatives(0);
        falseNegatives = evaluation.numFalseNegatives(0);
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

    public double getAreaUnderROC() {
        return areaUnderROC;
    }

    public double getKappa() {
        return kappa;
    }

    public double getTruePositives() {
        return truePositives;
    }

    public double getFalsePositives() {
        return falsePositives;
    }

    public double getTrueNegatives() {
        return trueNegatives;
    }

    public double getFalseNegatives() {
        return falseNegatives;
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

    public CustomClassifier getCustomClassifier() {
        return customClassifier;
    }
}
