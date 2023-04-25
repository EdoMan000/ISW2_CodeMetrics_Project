package metrics.controllers;

import metrics.models.ProjectClass;

import java.io.IOException;
import java.util.List;

public class ComputeMetrics {

    private final List<ProjectClass> allProjectClasses;
    private final ExtractInfoFromGit gitExtractor;
    public ComputeMetrics(ExtractInfoFromGit gitExtractor, List<ProjectClass> allProjectClasses) {
        this.allProjectClasses = allProjectClasses;
        this.gitExtractor = gitExtractor;
    }

    private void computeSize() {
        for(ProjectClass projectClass : allProjectClasses) {
            String[] lines = projectClass.getContentOfClass().split("\r\n|\r|\n");
            projectClass.getMetrics().setSize(lines.length);
        }
    }

    private void computeLOCMetrics() throws IOException {
        int addedLOC;
        int maxAddedLOC;
        int removedLOC;
        int maxRemovedLOC;
        int totalChurn;
        int maxChurn;
        int i;
        double avgAddedLOC;
        double avgRemovedLOC;
        double avgChurn;
        for(ProjectClass projectClass : allProjectClasses) {
            addedLOC = 0; removedLOC = 0; maxAddedLOC = 0; maxRemovedLOC = 0; totalChurn = 0; maxChurn = 0; avgAddedLOC = 0; avgRemovedLOC = 0; avgChurn = 0;
            gitExtractor.extractAddedOrRemovedLOC(projectClass);

            List<Integer> locAddedByClass = projectClass.getLOCAddedByClass();
            List<Integer> locRemovedByClass = projectClass.getLOCRemovedByClass();
            for(i = 0; i < locAddedByClass.size(); i++) {
                int addedLineOfCode = locAddedByClass.get(i);
                int removedLineOfCode = locRemovedByClass.get(i);
                int churningFactor = Math.abs(locAddedByClass.get(i) - locRemovedByClass.get(i));
                addedLOC += addedLineOfCode;
                removedLOC += removedLineOfCode;
                totalChurn += churningFactor;
                if(addedLineOfCode > maxAddedLOC) {
                    maxAddedLOC = addedLineOfCode;
                }
                if(removedLineOfCode > maxRemovedLOC) {
                    maxRemovedLOC = removedLineOfCode;
                }
                if(churningFactor > maxChurn) {
                    maxChurn = churningFactor;
                }
            }

            if(!locAddedByClass.isEmpty()) {
                avgAddedLOC = 1.0*addedLOC/ locAddedByClass.size();
            }
            if(!locRemovedByClass.isEmpty()) {
                avgRemovedLOC = 1.0*removedLOC/ locRemovedByClass.size();
            }
            if(!locAddedByClass.isEmpty() || !locRemovedByClass.isEmpty()) {
                avgChurn = 1.0*totalChurn/ (locAddedByClass.size() + locRemovedByClass.size());
            }

            projectClass.getMetrics().setAddedLOC(addedLOC);
            projectClass.getMetrics().setMaxAddedLOC(maxAddedLOC);
            projectClass.getMetrics().setAvgAddedLOC(avgAddedLOC);
            projectClass.getMetrics().setRemovedLOC(removedLOC);
            projectClass.getMetrics().setMaxRemovedLOC(maxRemovedLOC);
            projectClass.getMetrics().setAvgRemovedLOC(avgRemovedLOC);
            projectClass.getMetrics().setChurn(totalChurn);
            projectClass.getMetrics().setMaxChurningFactor(maxChurn);
            projectClass.getMetrics().setAvgChurningFactor(avgChurn);
        }
    }

    public void computeAllMetrics() throws IOException {
        computeSize();
        computeLOCMetrics();
    }
}
