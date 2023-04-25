package metrics.controllers;

import metrics.models.ProjectClass;
import metrics.models.Release;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CreateCsvWithMetrics {

    private CreateCsvWithMetrics() {}

    public static List<Integer> writeOnCsvFile(String projName, List<Release> releaseList, List<ProjectClass> allProjectClasses) {
        FileWriter fileWriter = null;
        List<Integer> buggyClassesList = new ArrayList<>();
        int count;
        try {
            fileWriter = new FileWriter(projName + "DataExtraction.csv");
            fileWriter.append("ReleaseID,File Name,Size,LOCAdded,AvgLOCAdded,MaxLOCAdded,LOCRemoved,AvgLOCRemoved,MaxLOCRemoved,Churn,AvgChurn,MaxChurn,#Commits,Buggy").append("\n");
            for (Release release: releaseList) {
                count = 0;
                for(ProjectClass projectClass: allProjectClasses){
                    if(projectClass.getRelease().id()==release.id()){
                        count = appendEntriesOnCSV(fileWriter, count, release, projectClass);
                    }
                }
                buggyClassesList.add(count);
            }
        } catch (Exception e) {
            System.out.println("Error in csv writer");
            e.printStackTrace();
        } finally {
            try {
                assert fileWriter != null;
                fileWriter.flush();
                fileWriter.close();
            } catch (IOException e) {
                System.out.println("Error while flushing/closing fileWriter !!!");
                e.printStackTrace();
            }

        }
        return buggyClassesList;
    }

    private static int appendEntriesOnCSV(FileWriter fileWriter, int count, Release release, ProjectClass projectClass) throws IOException {
        String releaseID = Integer.toString(release.id());
        String isClassBugged = projectClass.getMetrics().getBuggyness() ? "YES" : "NO" ;
        String sizeOfClass = String.valueOf(projectClass.getMetrics().getSize());
        String addedLOC = String.valueOf(projectClass.getMetrics().getAddedLOC());
        String avgAddedLOC = String.valueOf(projectClass.getMetrics().getAvgAddedLOC());
        String maxAddedLOC = String.valueOf(projectClass.getMetrics().getMaxAddedLOC());
        String removedLOC = String.valueOf(projectClass.getMetrics().getRemovedLOC());
        String avgRemovedLOC = String.valueOf(projectClass.getMetrics().getAvgRemovedLOC());
        String maxRemovedLOC = String.valueOf(projectClass.getMetrics().getMaxRemovedLOC());
        String churn = String.valueOf(projectClass.getMetrics().getChurn());
        String avgChurn = String.valueOf(projectClass.getMetrics().getAvgChurningFactor());
        String maxChurn = String.valueOf(projectClass.getMetrics().getMaxChurningFactor());
        String numOfCommitsThatTouchTheClass = String.valueOf(projectClass.getCommitsThatTouchTheClass().size());
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
                .append(numOfCommitsThatTouchTheClass).append(",")
                .append(isClassBugged).append("\n");
        return count;
    }
}
