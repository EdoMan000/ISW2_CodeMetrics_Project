package metrics.controllers;

import metrics.models.ProjectClass;
import metrics.models.Release;
import metrics.utilities.FileWriterUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

public class CreateArffOrCsvFiles {

    public static final String NAME_OF_THIS_CLASS = CreateArffOrCsvFiles.class.getName();
    private static final Logger logger = Logger.getLogger(NAME_OF_THIS_CLASS);

    private CreateArffOrCsvFiles() {}

    public static void writeOnArffFile(String projName, List<Release> releaseList, List<ProjectClass> allProjectClasses, String setType){
        FileWriter fileWriter = null;
        try {
            StringBuilder pathname = new StringBuilder();
            pathname.append("outputFiles/arffFiles/").append(projName).append("/").append(setType);
            File file = new File(pathname.toString());
            if (!file.exists()) {
                boolean success = file.mkdirs();
                if (!success) {
                    throw new IOException();
                }
            }
            StringBuilder iDs = new StringBuilder();
            StringBuilder pathName = new StringBuilder();
            pathName.append("/").append(setType);
            iDs.append("/Release");
            for(Release release: releaseList){
                iDs.append(release.id());
            }
            file = new File("outputFiles/arffFiles/" + projName + pathName + iDs + setType + "Set.arff");
            fileWriter = new FileWriter(file);
            fileWriter.append("@relation ").append(String.valueOf(iDs).replace("/","")).append(setType).append("Set\n")
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
                            @attribute NUMBER_OF_REVISIONS numeric
                            @attribute NUMBER_OF_DEFECT_FIXES numeric
                            @attribute NUMBER_OF_AUTHORS numeric
                            @attribute IS_BUGGY {'YES', 'NO'}
                            @data
                            """);
            for (Release release: releaseList) {
                for(ProjectClass projectClass: allProjectClasses){
                    if(projectClass.getRelease().id()==release.id()){
                        appendEntriesLikeCSV(fileWriter, release, projectClass, true);
                    }
                }
            }
        } catch (IOException e) {
            logger.info("Error in writeCsvOnFile when trying to create directory");
        } finally {
            assert fileWriter != null;
            FileWriterUtils.flushAndCloseFW(fileWriter, logger, NAME_OF_THIS_CLASS);
        }
    }

    public static void writeOnCsvFile(String projName, List<Release> releaseList, List<ProjectClass> allProjectClasses) {
        FileWriter fileWriter = null;
        try {
            File file = new File("outputFiles/csvFiles");
            if (!file.exists()) {
                boolean success = file.mkdirs();
                if (!success) {
                    throw new IOException();
                }
            }
            file = new File("outputFiles/csvFiles/" + projName + "DataExtraction.csv");
            fileWriter = new FileWriter(file);
            fileWriter.append("RELEASE_ID," +
                            "FILE_NAME," +
                            "SIZE," +
                            "LOC_ADDED,LOC_ADDED_AVG,LOC_ADDED_MAX," +
                            "LOC_REMOVED,LOC_REMOVED_AVG,LOC_REMOVED_MAX," +
                            "CHURN,CHURN_AVG,CHURN_MAX," +
                            "NUMBER_OF_REVISIONS," +
                            "NUMBER_OF_DEFECT_FIXES," +
                            "NUMBER_OF_AUTHORS," +
                            "IS_BUGGY").append("\n");
            for (Release release: releaseList) {
                for(ProjectClass projectClass: allProjectClasses){
                    if(projectClass.getRelease().id()==release.id()){
                        appendEntriesLikeCSV(fileWriter, release, projectClass, false);
                    }
                }
            }
        } catch (IOException e) {
            logger.info("Error in writeCsvOnFile when trying to create directory");
        } finally {
            assert fileWriter != null;
            FileWriterUtils.flushAndCloseFW(fileWriter, logger, NAME_OF_THIS_CLASS);
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
                .append(churn).append(",")
                .append(avgChurn).append(",")
                .append(maxChurn).append(",")
                .append(nRevisions).append(",")
                .append(nDefectFixes).append(",")
                .append(nAuthors).append(",")
                .append(isClassBugged).append("\n");
    }
}
