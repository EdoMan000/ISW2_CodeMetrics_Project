package metrics.controllers;

import metrics.models.AllResultsOfClassifiers;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

import java.util.logging.Logger;


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
                DataSource trainingSetDataSource = new DataSource(this.projName + "_" + howManyIterations + "_TrainingSet.arff");
                DataSource testingSetDataSource = new DataSource(this.projName + "_" + howManyIterations + "_TestingSet.arff");
                Instances trainingSetInstance = trainingSetDataSource.getDataSet();
                Instances testingSetInstance = testingSetDataSource.getDataSet();


            } catch (Exception e) {
                logger.info("Error in DataSource creation in wekaExtractor retrieveAllEvaluationsFromClassifiers() function");
            }
        }
        return allResultsOfClassifiers;
    }
}
