package metrics.controllers;

import metrics.models.CustomClassifier;
import weka.attributeSelection.GreedyStepwise;
import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.lazy.IBk;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.RandomForest;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.supervised.instance.ClassBalancer;
import weka.filters.supervised.instance.SpreadSubsample;

import java.util.ArrayList;
import java.util.List;

public class ComputeAllClassifiersCombinations {
    private ComputeAllClassifiersCombinations() {
    }

    public static List<CustomClassifier> returnAllClassifiersCombinations(){
        List<Classifier> classifierList = new ArrayList<>(List.of(new RandomForest(), new NaiveBayes(), new IBk())) ;
        List<AttributeSelection> featureSelectionFilters = getFeatureSelectionFilters();
        List<Filter> samplingFilters = getSamplingFilters() ;
        List<CustomClassifier> customClassifiersList = new ArrayList<>() ;
        //NO FEATURE SELECTION AND NO SAMPLING
        for (Classifier classifier : classifierList) {
            customClassifiersList.add(new CustomClassifier(classifier, classifier.getClass().getSimpleName(), "NoSelection", false, "NoSampling"));
        }
        //ONLY FEATURE SELECTION
        for (AttributeSelection featureSelectionFilter : featureSelectionFilters) {
            for (Classifier classifier : classifierList) {
                FilteredClassifier filteredClassifier = new FilteredClassifier() ;
                filteredClassifier.setClassifier(classifier);
                filteredClassifier.setFilter(featureSelectionFilter);

                customClassifiersList.add(new CustomClassifier(filteredClassifier, classifier.getClass().getSimpleName(), featureSelectionFilter.getSearch().getClass().getSimpleName(), ((GreedyStepwise)featureSelectionFilter.getSearch()).getSearchBackwards(), "NoSampling"));
            }
        }
        //ONLY SAMPLING
        for (Filter samplingFilter : samplingFilters) {
            for (Classifier classifier : classifierList) {
                FilteredClassifier filteredClassifier = new FilteredClassifier() ;
                filteredClassifier.setClassifier(classifier);
                filteredClassifier.setFilter(samplingFilter);

                customClassifiersList.add(new CustomClassifier(filteredClassifier, classifier.getClass().getSimpleName(),"NoSelection", false, samplingFilter.getClass().getSimpleName()));
            }
        }
        //FEATURE SELECTION AND SAMPLING
        for (AttributeSelection featureSelectionFilter : featureSelectionFilters) {
            for (Filter samplingFilter : samplingFilters) {
                for (Classifier classifier : classifierList) {
                    FilteredClassifier innerClassifier = new FilteredClassifier() ;
                    innerClassifier.setClassifier(classifier);
                    innerClassifier.setFilter(samplingFilter);

                    FilteredClassifier externalClassifier = new FilteredClassifier() ;
                    externalClassifier.setFilter(featureSelectionFilter);
                    externalClassifier.setClassifier(innerClassifier);

                    customClassifiersList.add(new CustomClassifier(externalClassifier, classifier.getClass().getSimpleName(), featureSelectionFilter.getSearch().getClass().getSimpleName(), ((GreedyStepwise)featureSelectionFilter.getSearch()).getSearchBackwards(), samplingFilter.getClass().getSimpleName()));
                }
            }
        }


        return customClassifiersList ;
    }

    private static List<Filter> getSamplingFilters() {
        List<Filter> filterList = new ArrayList<>() ;
        ClassBalancer classBalancer = new ClassBalancer() ;
        filterList.add(classBalancer) ;
        SpreadSubsample spreadSubsample = new SpreadSubsample() ;
        spreadSubsample.setDistributionSpread(1.0);
        filterList.add(spreadSubsample);
        return filterList ;
    }

    private static List<AttributeSelection> getFeatureSelectionFilters() {
        List<AttributeSelection> filterList = new ArrayList<>() ;
        for (int i = 0 ; i < 2 ; i++) {
            AttributeSelection attributeSelection = new AttributeSelection() ;
            GreedyStepwise greedyStepwise = new GreedyStepwise() ;
            greedyStepwise.setSearchBackwards(i != 0);
            attributeSelection.setSearch(greedyStepwise);
            filterList.add(attributeSelection) ;
        }
        return filterList ;
    }
}
