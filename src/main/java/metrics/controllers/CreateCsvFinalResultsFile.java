package metrics.controllers;

import metrics.models.ResultOfClassifier;
import metrics.utilities.FileWriterUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

public class CreateCsvFinalResultsFile {


    public static final String NAME_OF_THIS_CLASS = CreateCsvFinalResultsFile.class.getName();
    private static final Logger logger = Logger.getLogger(NAME_OF_THIS_CLASS);

    private CreateCsvFinalResultsFile() {}

    public static void writeCsvFinalResultsFile(String projName, List<ResultOfClassifier> finalResultsList, String type){
        try {
            File file = new File("outputFiles/FinalResults/" + projName );
            if (!file.exists()) {
                boolean success = file.mkdirs();
                if (!success) {
                    throw new IOException();
                }
            }
            StringBuilder fileName = new StringBuilder();
            fileName.append("/").append(projName).append("_finalReport_").append(type).append(".csv");
            file = new File("outputFiles/FinalResults/" + projName + fileName);
            try(FileWriter fileWriter = new FileWriter(file)) {
                fileWriter.append("DATASET," +
                        "#TRAINING_RELEASES," +
                        "%TRAINING_INSTANCES," +
                        "CLASSIFIER," +
                        "FEATURE_SELECTION," +
                        "BALANCING," +
                        "PRECISION," +
                        "RECALL," +
                        "AREA_UNDER_ROC," +
                        "KAPPA," +
                        "TRUE_POSITIVES," +
                        "FALSE_POSITIVES," +
                        "TRUE_NEGATIVES," +
                        "FALSE_NEGATIVES").append("\n");
                for(ResultOfClassifier resultOfClassifier: finalResultsList){
                    fileWriter.append(projName).append(",");
                    if(type.equals("completeInfo")){
                        fileWriter.append(String.valueOf(resultOfClassifier.getWalkForwardIteration())).append(",")
                                .append(String.valueOf(resultOfClassifier.getTrainingPercent())).append(",");
                    }else {
                        fileWriter.append("None").append(",")
                                .append("None").append(",");
                    }
                    fileWriter.append(resultOfClassifier.getClassifierName()).append(",");
                    if(resultOfClassifier.hasFeatureSelection()){
                        fileWriter.append("Greedy backward search").append(",");
                    }else {
                        fileWriter.append("None").append(",");
                    }
                    if(resultOfClassifier.hasSampling()){
                        fileWriter.append("Undersampling").append(",");
                    }else {
                        fileWriter.append("None").append(",");
                    }
                    fileWriter.append(String.valueOf(resultOfClassifier.getPrecision())).append(",")
                            .append(String.valueOf(resultOfClassifier.getRecall())).append(",")
                            .append(String.valueOf(resultOfClassifier.getAreaUnderROC())).append(",")
                            .append(String.valueOf(resultOfClassifier.getKappa())).append(",")
                            .append(String.valueOf(resultOfClassifier.getTruePositives())).append(",")
                            .append(String.valueOf(resultOfClassifier.getFalsePositives())).append(",")
                            .append(String.valueOf(resultOfClassifier.getTrueNegatives())).append(",")
                            .append(String.valueOf(resultOfClassifier.getFalseNegatives())).append("\n");
                }
                FileWriterUtils.flushAndCloseFW(fileWriter, logger, NAME_OF_THIS_CLASS);
            }
        } catch (IOException e) {
            logger.info("Error in .csv creation when trying to create directory");
        }
    }

}
