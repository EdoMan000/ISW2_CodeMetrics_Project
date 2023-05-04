package metrics.models;

import weka.classifiers.Classifier;

public class CustomClassifier {
    private final Classifier classifier;
    private final String featureSelectionFilterName;
    private final String samplingFilterName;
    private final String classifierName;

    public CustomClassifier(Classifier classifier, String classifierName, String featureSelectionFilterName, String bestFirstDirection, String samplingFilterName) {
        this.classifier = classifier;
        if(samplingFilterName.equals("Resample")){
            this.samplingFilterName = "OverSampling";
        } else if (samplingFilterName.equals("SpreadSubsample")) {
            this.samplingFilterName = "UnderSampling";
        } else if (samplingFilterName.equals("SMOTE")) {
            this.samplingFilterName = "SyntheticMinorityOversampling";
        } else {
            this.samplingFilterName = samplingFilterName;
        }
        if(featureSelectionFilterName.equals("BestFirst")){
            this.featureSelectionFilterName = featureSelectionFilterName + "(" + bestFirstDirection + ")";
        } else {
            this.featureSelectionFilterName = featureSelectionFilterName;
        }
        this.classifierName = classifierName;
    }


    public Classifier getClassifier() {
        return classifier;
    }

    public String getClassifierName() {
        return classifierName;
    }

    public String getFeatureSelectionFilterName() {
        return featureSelectionFilterName;
    }

    public String getSamplingFilterName() {
        return samplingFilterName;
    }
}
