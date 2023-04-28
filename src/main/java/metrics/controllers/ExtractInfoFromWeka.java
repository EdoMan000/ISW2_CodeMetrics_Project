package metrics.controllers;

import metrics.models.AllResultsOfClassifiers;
import weka.core.converters.ConverterUtils.DataSource;

import java.io.IOException;


public class ExtractInfoFromWeka {
    private final String projName;
    private final int howManyIterations;

    public ExtractInfoFromWeka(String projName, int howManyIterations) {
        this.projName = projName;
        this.howManyIterations = howManyIterations;
    }

    public AllResultsOfClassifiers retrieveAllEvaluationsFromClassifiers() throws IOException {
        AllResultsOfClassifiers allResultsOfClassifiers = new AllResultsOfClassifiers();
        for(int i = 1;i <= howManyIterations;i++){
            try {
                DataSource trainingSet = new DataSource(this.projName + "_" + howManyIterations + "_TrainingSet.arff");
                DataSource testingSet = new DataSource(this.projName + "_" + howManyIterations + "_TestingSet.arff");
            } catch (Exception e) {
                throw new IOException();
            }
        }
        return allResultsOfClassifiers;
    }
}
