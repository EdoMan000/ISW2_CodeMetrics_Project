package metrics.controllers;

import metrics.models.ProjectClass;
import metrics.models.Release;
import metrics.utilities.FileWriterUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class CreateCsvWithMetrics {

    public static final String NAME_OF_THIS_CLASS = CreateCsvWithMetrics.class.getName();
    private static final Logger logger = Logger.getLogger(NAME_OF_THIS_CLASS);

    private CreateCsvWithMetrics() {}

    public static List<Integer> writeOnCsvFile(String projName, List<Release> releaseList, List<ProjectClass> allProjectClasses) {
        FileWriter fileWriter;
        List<Integer> buggyClassesList = new ArrayList<>();
        int count;
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
            fileWriter.append("ReleaseID,File Name,Size,LOCAdded,AvgLOCAdded,MaxLOCAdded,LOCRemoved,AvgLOCRemoved,MaxLOCRemoved,Churn,AvgChurn,MaxChurn,NR,Nfix,NAuth,Buggy").append("\n");
            for (Release release: releaseList) {
                count = 0;
                for(ProjectClass projectClass: allProjectClasses){
                    if(projectClass.getRelease().id()==release.id()){
                        count = appendEntriesOnCSV(fileWriter, count, release, projectClass);
                    }
                }
                buggyClassesList.add(count);
            }
            FileWriterUtils.flushAndCloseFW(fileWriter, logger, NAME_OF_THIS_CLASS);
        } catch (IOException e) {
            logger.info("Error in writeCsvOnFile when trying to create directory");
        }
        return buggyClassesList;
    }

    private static int appendEntriesOnCSV(FileWriter fileWriter, int count, Release release, ProjectClass projectClass) throws IOException {
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
        if (isClassBugged.equals("YES")) {
            count++;
        }
        String className = projectClass.getName();
        fileWriter.append(releaseID).append(",")
                .append(className).append(",")
                .append(sizeOfClass).append(",")
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
        return count;
    }
}
