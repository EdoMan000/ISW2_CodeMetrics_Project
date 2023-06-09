package metrics.controllers;

import metrics.models.ProjectClass;
import metrics.models.Release;
import metrics.utilities.FileWriterUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

public class CreateArffOrCsvDataFiles {

    public static final String NAME_OF_THIS_CLASS = CreateArffOrCsvDataFiles.class.getName();
    private static final Logger logger = Logger.getLogger(NAME_OF_THIS_CLASS);

    private CreateArffOrCsvDataFiles() {}

    public static void writeOnArffOrCsvDataFile(String projName, List<Release> releaseList, List<ProjectClass> allProjectClasses, String setType, boolean isArff, int iterationNumber){
        try {
            StringBuilder folderName = new StringBuilder();
            if (isArff) {
                folderName.append("outputFiles/arffFiles/").append(projName).append("/").append(setType);
            }else{
                folderName.append("outputFiles/csvFiles/").append(projName).append("/").append(setType);
            }
            File file = new File(folderName.toString());
            if (!file.exists()) {
                boolean success = file.mkdirs();
                if (!success) {
                    throw new IOException();
                }
            }
            StringBuilder pathName = new StringBuilder();
            pathName.append("/").append(setType).append("/").append(projName).append("_").append(iterationNumber).append("_");
            StringBuilder fileName = new StringBuilder();
            fileName.append(projName).append("_").append(iterationNumber).append("_").append(setType).append("Set");
            if (isArff) {
                fileName.append(".arff");
                file = new File("outputFiles/arffFiles/" + projName + pathName + setType + "Set.arff");
            }else{
                fileName.append(".csv");
                file = new File("outputFiles/csvFiles/" + projName + pathName + setType + "Set.csv");
            }
            try(FileWriter fileWriter = new FileWriter(file)) {
                appendOnFile(releaseList, allProjectClasses, isArff, fileName.toString(), fileWriter);
            }
        } catch (IOException e) {
            if (isArff) {
                logger.info("Error in .arff creation when trying to create directory");

            }else{
                logger.info("Error in .csv creation when trying to create directory");
            }
        }
    }

    private static void appendOnFile(List<Release> releaseList, List<ProjectClass> allProjectClasses, boolean isArff, String fileName, FileWriter fileWriter) throws IOException {
        if(isArff){
            fileWriter.append("@relation ").append(fileName).append("\n\n")
                    .append("""
                        @attribute SIZE numeric
                        @attribute LOC_ADDED numeric
                        @attribute LOC_ADDED_AVG numeric
                        @attribute LOC_ADDED_MAX numeric
                        @attribute LOC_REMOVED numeric
                        @attribute LOC_REMOVED_AVG numeric
                        @attribute LOC_REMOVED_MAX numeric
                        @attribute CHURN numeric
                        @attribute CHURN_AVG numeric
                        @attribute CHURN_MAX numeric
                        @attribute LOC_TOUCHED numeric
                        @attribute LOC_TOUCHED_AVG numeric
                        @attribute LOC_TOUCHED_MAX numeric
                        @attribute NUMBER_OF_REVISIONS numeric
                        @attribute NUMBER_OF_DEFECT_FIXES numeric
                        @attribute NUMBER_OF_AUTHORS numeric
                        @attribute IS_BUGGY {'YES', 'NO'}
                        
                        @data
                        """);
            appendByRelease(releaseList, allProjectClasses, fileWriter, true);
        }else{
            fileWriter.append("RELEASE_ID," +
                    "FILE_NAME," +
                    "SIZE," +
                    "LOC_ADDED,LOC_ADDED_AVG,LOC_ADDED_MAX," +
                    "LOC_REMOVED,LOC_REMOVED_AVG,LOC_REMOVED_MAX," +
                    "LOC_TOUCHED,LOC_TOUCHED_AVG,LOC_TOUCHED_MAX," +
                    "CHURN,CHURN_AVG,CHURN_MAX," +
                    "NUMBER_OF_REVISIONS," +
                    "NUMBER_OF_DEFECT_FIXES," +
                    "NUMBER_OF_AUTHORS," +
                    "IS_BUGGY").append("\n");
            appendByRelease(releaseList, allProjectClasses, fileWriter, false);
        }
        FileWriterUtils.flushAndCloseFW(fileWriter, logger, NAME_OF_THIS_CLASS);
    }

    private static void appendByRelease(List<Release> releaseList, List<ProjectClass> allProjectClasses, FileWriter fileWriter, boolean isArff) throws IOException {
        for (Release release : releaseList) {
            for (ProjectClass projectClass : allProjectClasses) {
                if (projectClass.getRelease().id() == release.id()) {
                    appendEntriesLikeCSV(fileWriter, release, projectClass, isArff);
                }
            }
        }
    }

    private static void appendEntriesLikeCSV(FileWriter fileWriter, Release release, ProjectClass projectClass, boolean isArff) throws IOException {
        String releaseID = Integer.toString(release.id());
        String isClassBugged = projectClass.getMetrics().getBuggyness() ? "YES" : "NO" ;
        String sizeOfClass = String.valueOf(projectClass.getMetrics().getSize());
        String addedLOC = String.valueOf(projectClass.getMetrics().getAddedLOCMetrics().getVal());
        String avgAddedLOC = String.valueOf(projectClass.getMetrics().getAddedLOCMetrics().getAvgVal());
        String maxAddedLOC = String.valueOf(projectClass.getMetrics().getAddedLOCMetrics().getMaxVal());
        String removedLOC = String.valueOf(projectClass.getMetrics().getRemovedLOCMetrics().getVal());
        String avgRemovedLOC = String.valueOf(projectClass.getMetrics().getRemovedLOCMetrics().getAvgVal());
        String maxRemovedLOC = String.valueOf(projectClass.getMetrics().getRemovedLOCMetrics().getMaxVal());
        String touchedLOC = String.valueOf(projectClass.getMetrics().getTouchedLOCMetrics().getVal());
        String avgTouchedLOC = String.valueOf(projectClass.getMetrics().getTouchedLOCMetrics().getAvgVal());
        String maxTouchedLOC = String.valueOf(projectClass.getMetrics().getTouchedLOCMetrics().getMaxVal());
        String churn = String.valueOf(projectClass.getMetrics().getChurnMetrics().getVal());
        String avgChurn = String.valueOf(projectClass.getMetrics().getChurnMetrics().getAvgVal());
        String maxChurn = String.valueOf(projectClass.getMetrics().getChurnMetrics().getMaxVal());
        String nRevisions = String.valueOf(projectClass.getMetrics().getNumberOfRevisions());
        String nDefectFixes = String.valueOf(projectClass.getMetrics().getNumberOfDefectFixes());
        String nAuthors = String.valueOf(projectClass.getMetrics().getNumberOfAuthors());

        String className = projectClass.getName();
        if(!isArff){
            fileWriter.append(releaseID).append(",")
                    .append(className).append(",");
        }
        fileWriter.append(sizeOfClass).append(",")
                .append(addedLOC).append(",")
                .append(avgAddedLOC).append(",")
                .append(maxAddedLOC).append(",")
                .append(removedLOC).append(",")
                .append(avgRemovedLOC).append(",")
                .append(maxRemovedLOC).append(",")
                .append(touchedLOC).append(",")
                .append(avgTouchedLOC).append(",")
                .append(maxTouchedLOC).append(",")
                .append(churn).append(",")
                .append(avgChurn).append(",")
                .append(maxChurn).append(",")
                .append(nRevisions).append(",")
                .append(nDefectFixes).append(",")
                .append(nAuthors).append(",")
                .append(isClassBugged).append("\n");
    }
}
