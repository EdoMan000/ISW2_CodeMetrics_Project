package metrics.controllers;

import metrics.models.CustomClassifier;
import weka.attributeSelection.BestFirst;
import weka.classifiers.Classifier;
import weka.classifiers.CostMatrix;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.lazy.IBk;
import weka.classifiers.meta.CostSensitiveClassifier;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.RandomForest;
import weka.core.AttributeStats;
import weka.core.SelectedTag;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.supervised.instance.Resample;
import weka.filters.supervised.instance.SMOTE;
import weka.filters.supervised.instance.SpreadSubsample;

import java.util.ArrayList;
import java.util.List;

public class ComputeAllClassifiersCombinations {
    private ComputeAllClassifiersCombinations() {
    }

    public static List<CustomClassifier> returnAllClassifiersCombinations(AttributeStats isBuggyAttributeStats){
        List<Classifier> classifierList = new ArrayList<>(List.of(new RandomForest(), new NaiveBayes(), new IBk())) ;
        List<AttributeSelection> featureSelectionFilters = getFeatureSelectionFilters();
        int majorityClassSize = isBuggyAttributeStats.nominalCounts[1];
        int minorityClassSize = isBuggyAttributeStats.nominalCounts[0];
        List<Filter> samplingFilters = getSamplingFilters(majorityClassSize, minorityClassSize);
        List<CustomClassifier> customClassifiersList = new ArrayList<>();
        //NO FEATURE SELECTION NO SAMPLING NO COST SENSITIVE
        for (Classifier classifier : classifierList) {
            customClassifiersList.add(new CustomClassifier(classifier, classifier.getClass().getSimpleName(), "NoSelection", null, "NoSampling", false));
        }
        //ONLY FEATURE SELECTION
        for (AttributeSelection featureSelectionFilter : featureSelectionFilters) {
            for (Classifier classifier : classifierList) {
                FilteredClassifier filteredClassifier = new FilteredClassifier();
                filteredClassifier.setClassifier(classifier);
                filteredClassifier.setFilter(featureSelectionFilter);

                customClassifiersList.add(new CustomClassifier(filteredClassifier, classifier.getClass().getSimpleName(), featureSelectionFilter.getSearch().getClass().getSimpleName(), ((BestFirst)featureSelectionFilter.getSearch()).getDirection().getSelectedTag().getReadable(), "NoSampling", false));
            }
        }
        //ONLY SAMPLING
        for (Filter samplingFilter : samplingFilters) {
            for (Classifier classifier : classifierList) {
                FilteredClassifier filteredClassifier = new FilteredClassifier();
                filteredClassifier.setClassifier(classifier);
                filteredClassifier.setFilter(samplingFilter);

                customClassifiersList.add(new CustomClassifier(filteredClassifier, classifier.getClass().getSimpleName(),"NoSelection", null, samplingFilter.getClass().getSimpleName(), false));
            }
        }
        //ONLY COST SENSITIVE
        for (Classifier classifier : classifierList) {
            List<CostSensitiveClassifier> costSensitiveFilters = getCostSensitiveFilters();
            for (CostSensitiveClassifier costSensitiveClassifier : costSensitiveFilters) {
                costSensitiveClassifier.setClassifier(classifier);

                customClassifiersList.add(new CustomClassifier(costSensitiveClassifier, classifier.getClass().getSimpleName(),"NoSelection", null, "NoSampling", true));
            }
        }
        //FEATURE SELECTION AND SAMPLING
        for (AttributeSelection featureSelectionFilter : featureSelectionFilters) {
            for (Filter samplingFilter : samplingFilters) {
                for (Classifier classifier : classifierList) {
                    FilteredClassifier innerClassifier = new FilteredClassifier();
                    innerClassifier.setClassifier(classifier);
                    innerClassifier.setFilter(samplingFilter);

                    FilteredClassifier externalClassifier = new FilteredClassifier();
                    externalClassifier.setFilter(featureSelectionFilter);
                    externalClassifier.setClassifier(innerClassifier);

                    customClassifiersList.add(new CustomClassifier(externalClassifier, classifier.getClass().getSimpleName(), featureSelectionFilter.getSearch().getClass().getSimpleName(), ((BestFirst)featureSelectionFilter.getSearch()).getDirection().getSelectedTag().getReadable(), samplingFilter.getClass().getSimpleName(), false));
                }
            }
        }
        //FEATURE SELECTION AND COST SENSITIVE
        for (Classifier classifier : classifierList) {
            List<CostSensitiveClassifier> costSensitiveFilters = getCostSensitiveFilters();
            for(CostSensitiveClassifier costSensitiveClassifier: costSensitiveFilters){
                for (AttributeSelection featureSelectionFilter : featureSelectionFilters) {
                    FilteredClassifier filteredClassifier = new FilteredClassifier();
                    filteredClassifier.setFilter(featureSelectionFilter);
                    costSensitiveClassifier.setClassifier(classifier);
                    filteredClassifier.setClassifier(costSensitiveClassifier);

                    customClassifiersList.add(new CustomClassifier(filteredClassifier, classifier.getClass().getSimpleName(), featureSelectionFilter.getSearch().getClass().getSimpleName(), ((BestFirst)featureSelectionFilter.getSearch()).getDirection().getSelectedTag().getReadable(), "NoSampling", true));
                }
            }
        }
        return customClassifiersList;
    }

    private static List<Filter> getSamplingFilters(int majorityClassSize, int minorityClassSize) {
        double percentStandardOversampling = ((100.0*majorityClassSize)/(majorityClassSize + minorityClassSize))*2;
        double percentSMOTE;
        if(minorityClassSize==0 || minorityClassSize > majorityClassSize){
            percentSMOTE = 0;
        }else{
            percentSMOTE = (100.0*(majorityClassSize-minorityClassSize))/minorityClassSize;
        }
        List<Filter> filterList = new ArrayList<>();
        Resample resample = new Resample();
        resample.setBiasToUniformClass(1.0);
        resample.setSampleSizePercent(percentStandardOversampling);
        filterList.add(resample);
        SpreadSubsample spreadSubsample = new SpreadSubsample();
        spreadSubsample.setDistributionSpread(1.0);
        filterList.add(spreadSubsample);
        SMOTE smote = new SMOTE();
        smote.setClassValue("1");
        smote.setPercentage(percentSMOTE);
        filterList.add(smote);
        return filterList;
    }

    private static List<AttributeSelection> getFeatureSelectionFilters() {
        List<AttributeSelection> filterList = new ArrayList<>();
        for (int i = 0 ; i < 3 ; i++) {
            AttributeSelection attributeSelection = new AttributeSelection();
            BestFirst bestFirst = new BestFirst();
            bestFirst.setDirection(new SelectedTag(i, bestFirst.getDirection().getTags()));
            attributeSelection.setSearch(bestFirst);
            filterList.add(attributeSelection);
        }
        return filterList;
    }

    private static List<CostSensitiveClassifier> getCostSensitiveFilters() {
        CostSensitiveClassifier costSensitiveClassifier = new CostSensitiveClassifier();
        costSensitiveClassifier.setMinimizeExpectedCost(true);
        CostMatrix costMatrix = getCostMatrix();
        costSensitiveClassifier.setCostMatrix(costMatrix);
        return new ArrayList<>(List.of(costSensitiveClassifier));
    }

    private static CostMatrix getCostMatrix() {
        double weightFalsePositive = 1.0;
        double weightFalseNegative = 10.0;
        CostMatrix costMatrix = new CostMatrix(2);
        costMatrix.setCell(0, 0, 0.0);
        costMatrix.setCell(1, 0, weightFalsePositive);
        costMatrix.setCell(0, 1, weightFalseNegative);
        costMatrix.setCell(1, 1, 0.0);
        return costMatrix;
    }
}
