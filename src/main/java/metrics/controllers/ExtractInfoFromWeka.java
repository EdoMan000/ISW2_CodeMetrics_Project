package metrics.controllers;

import metrics.models.CustomClassifier;
import metrics.models.ResultOfClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;


public class ExtractInfoFromWeka {
    private final String projName;
    private final int howManyIterations;
    private final Logger logger = Logger.getLogger(ExtractInfoFromWeka.class.getName());

    public ExtractInfoFromWeka(String projName, int howManyIterations) {
        this.projName = projName;
        this.howManyIterations = howManyIterations;
    }

    public List<ResultOfClassifier> retrieveAllResultsFromClassifiers() {
        List<ResultOfClassifier> allResultsOfClassifiers = new ArrayList<>();
        for(int walkForwardIteration = 1;walkForwardIteration <= howManyIterations;walkForwardIteration++){
            try {
                DataSource trainingSetDataSource = new DataSource("outputFiles/arffFiles/" + this.projName + "/Training/" + this.projName + "_" + walkForwardIteration + "_TrainingSet.arff");
                DataSource testingSetDataSource = new DataSource("outputFiles/arffFiles/" + this.projName + "/Testing/" + this.projName + "_" + walkForwardIteration + "_TestingSet.arff");
                Instances trainingSetInstance = trainingSetDataSource.getDataSet();
                Instances testingSetInstance = testingSetDataSource.getDataSet();

                int numAttr = trainingSetInstance.numAttributes();
                trainingSetInstance.setClassIndex(numAttr - 1);
                testingSetInstance.setClassIndex(numAttr - 1);


                List<CustomClassifier> customClassifiers = ComputeAllClassifiersCombinations.returnAllClassifiersCombinations();

                for (CustomClassifier customClassifier : customClassifiers) {
                    Evaluation evaluator = new Evaluation(testingSetInstance) ;
                    Classifier classifier = customClassifier.getClassifier();
                    classifier.buildClassifier(trainingSetInstance);
                    evaluator.evaluateModel(classifier, testingSetInstance);
                    ResultOfClassifier resultOfClassifier = new ResultOfClassifier(walkForwardIteration, customClassifier, evaluator);
                    resultOfClassifier.setTrainingPercent(100.0 * trainingSetInstance.numInstances() / (trainingSetInstance.numInstances() + testingSetInstance.numInstances()));
                    allResultsOfClassifiers.add(resultOfClassifier);
                }
            } catch (Exception e) {
                logger.info("Error in wekaExtractor retrieveAllEvaluationsFromClassifiers() function");
            }
        }
        return allResultsOfClassifiers;
    }
}
