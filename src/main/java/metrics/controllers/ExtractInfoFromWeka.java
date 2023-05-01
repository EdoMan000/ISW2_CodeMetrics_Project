package metrics.controllers;

import metrics.models.AllResultsOfClassifiers;
import metrics.models.ResultOfClassifier;
import weka.attributeSelection.CfsSubsetEval;
import weka.attributeSelection.GreedyStepwise;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.lazy.IBk;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.supervised.instance.SpreadSubsample;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static metrics.utilities.ResultOfClassifierUtils.computeAvgOfAllLists;


public class ExtractInfoFromWeka {
    private final String projName;
    private final int howManyIterations;
    private final Logger logger = Logger.getLogger(ExtractInfoFromWeka.class.getName());

    public ExtractInfoFromWeka(String projName, int howManyIterations) {
        this.projName = projName;
        this.howManyIterations = howManyIterations;
    }

    public AllResultsOfClassifiers retrieveAllEvaluationsFromClassifiers() {
        AllResultsOfClassifiers allResultsOfClassifiers = new AllResultsOfClassifiers();
        for(int i = 1;i <= howManyIterations;i++){
            try {
                DataSource trainingSetDataSource = new DataSource("outputFiles/arffFiles/" + this.projName + "/Training/" + this.projName + "_" + i + "_TrainingSet.arff");
                DataSource testingSetDataSource = new DataSource("outputFiles/arffFiles/" + this.projName + "/Testing/" + this.projName + "_" + i + "_TestingSet.arff");
                Instances trainingSetInstance = trainingSetDataSource.getDataSet();
                Instances testingSetInstance = testingSetDataSource.getDataSet();

                RandomForest randomForestClassifier = new RandomForest();
                NaiveBayes naiveBayesClassifier = new NaiveBayes();
                IBk iBkClassifier = new IBk();
                List<Classifier> classifiers = new ArrayList<>();
                classifiers.add(randomForestClassifier);
                classifiers.add(naiveBayesClassifier);
                classifiers.add(iBkClassifier);

                int numAttr = trainingSetInstance.numAttributes();
                trainingSetInstance.setClassIndex(numAttr - 1);
                testingSetInstance.setClassIndex(numAttr - 1);

                Evaluation evaluation = new Evaluation(testingSetInstance);

                //NO FEATURE SELECTION AND NO SAMPLING
                doEvaluation(allResultsOfClassifiers.getAllBasicLists(), i, trainingSetInstance, testingSetInstance,
                        classifiers, evaluation, false, false);
                doEvaluation(allResultsOfClassifiers.getAllFeatureSelectionLists(), i, trainingSetInstance, testingSetInstance,
                        classifiers, evaluation, true, false);
                doEvaluation(allResultsOfClassifiers.getAllSamplingLists(), i, trainingSetInstance, testingSetInstance,
                        classifiers, evaluation, false, true);
                doEvaluation(allResultsOfClassifiers.getAllCompleteLists(), i, trainingSetInstance, testingSetInstance,
                        classifiers, evaluation, true, true);
            } catch (Exception e) {
                logger.info("Error in wekaExtractor retrieveAllEvaluationsFromClassifiers() function");
            }
        }
        computeAvgOfAllLists(allResultsOfClassifiers);
        allResultsOfClassifiers.createMergeOfResults();
        return allResultsOfClassifiers;
    }

    private void doEvaluation(List<List<ResultOfClassifier>> lists, int iterationNumber, Instances trainingSetInstance, Instances testingSetInstance, List<Classifier> classifiers, Evaluation evaluation, boolean hasFeatureSelection, boolean hasSampling) throws Exception {
        CfsSubsetEval subsetEval;
        GreedyStepwise search;
        AttributeSelection filter;
        Instances filteredTrainingInstance = null;
        Instances filteredTestingInstance = null;
        if(hasFeatureSelection) {
            subsetEval = new CfsSubsetEval();
            search = new GreedyStepwise();
            search.setSearchBackwards(true);

            filter = new AttributeSelection();
            filter.setEvaluator(subsetEval);
            filter.setSearch(search);
            filter.setInputFormat(trainingSetInstance);

            filteredTrainingInstance = Filter.useFilter(trainingSetInstance, filter);
            filteredTestingInstance = Filter.useFilter(testingSetInstance, filter);

            int numAttrFiltered = filteredTrainingInstance.numAttributes();
            filteredTrainingInstance.setClassIndex(numAttrFiltered - 1);
        }

        for(int i = 0; i<3; i++) {
            ResultOfClassifier resultOfClassifier;
            if(hasFeatureSelection){
                resultOfClassifier = initializeResultOfClassifier(iterationNumber, classifiers, evaluation, true, hasSampling, filteredTrainingInstance, filteredTestingInstance, i);
            }else{
                resultOfClassifier = initializeResultOfClassifier(iterationNumber, classifiers, evaluation, false, hasSampling, trainingSetInstance, testingSetInstance, i);
            }
            resultOfClassifier.setPrecision(evaluation.precision(0));
            resultOfClassifier.setRecall(evaluation.recall(0));
            resultOfClassifier.setAreaUnderROC(evaluation.areaUnderROC(0));
            resultOfClassifier.setKappa(evaluation.kappa());
            resultOfClassifier.setTruePositives(evaluation.numTruePositives(0));
            resultOfClassifier.setFalsePositives(evaluation.numFalsePositives(0));
            resultOfClassifier.setTrueNegatives(evaluation.numTrueNegatives(0));
            resultOfClassifier.setFalseNegatives(evaluation.numFalseNegatives(0));
            lists.get(i).add(resultOfClassifier);
        }
    }

    private ResultOfClassifier initializeResultOfClassifier(int iterationNumber, List<Classifier> classifiers, Evaluation evaluation, boolean hasFeatureSelection, boolean hasSampling, Instances training, Instances testing, int i) throws Exception {
        if(hasSampling){
            SpreadSubsample spreadSubsample = new SpreadSubsample();
            spreadSubsample.setInputFormat(training);
            spreadSubsample.setOptions(new String[] {"-M", "1.0"});
            FilteredClassifier filteredClassifier = new FilteredClassifier();
            filteredClassifier.setFilter(spreadSubsample);
            filteredClassifier.setClassifier(classifiers.get(i));
            filteredClassifier.buildClassifier(training);
            evaluation.evaluateModel(filteredClassifier, testing);
        }else{
            classifiers.get(i).buildClassifier(training);
            evaluation.evaluateModel(classifiers.get(i), testing);
        }
        ResultOfClassifier resultOfClassifier;
        resultOfClassifier = new ResultOfClassifier(projName, iterationNumber, classifiers.get(i).getClass().getName(), hasFeatureSelection, hasSampling);
        resultOfClassifier.setTrainingPercent(100.0 * training.numInstances() / (training.numInstances() + testing.numInstances()));
        return resultOfClassifier;
    }
}
