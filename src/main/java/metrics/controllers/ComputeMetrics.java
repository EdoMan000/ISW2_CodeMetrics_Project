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
        int addedLOC; int maxLOC; int totalChurn; int maxChurn; int i;
        double avgLOC; double avgChurn;
        for(ProjectClass projectClass : allProjectClasses) {
            addedLOC = 0; maxLOC = 0; totalChurn = 0; maxChurn = 0; avgLOC = 0; avgChurn = 0;
            gitExtractor.extractAddedOrRemovedLOC(projectClass);

            for(i = 0; i < projectClass.getLOCAddedByClass().size(); i++) {
                int lineOfCode = projectClass.getLOCAddedByClass().get(i);
                int churningFactor = Math.abs(projectClass.getLOCAddedByClass().get(i) - projectClass.getLOCRemovedByClass().get(i));
                addedLOC = addedLOC + lineOfCode;
                totalChurn = totalChurn + churningFactor;
                if(lineOfCode > maxLOC) {
                    maxLOC = lineOfCode;
                }
                if(churningFactor > maxChurn) {
                    maxChurn = churningFactor;
                }
            }

            if(!projectClass.getLOCAddedByClass().isEmpty()) {
                avgLOC = 1.0*addedLOC/projectClass.getLOCAddedByClass().size();
            }
            if(!projectClass.getLOCAddedByClass().isEmpty()) {
                avgChurn = 1.0*totalChurn/projectClass.getLOCAddedByClass().size();
            }

            projectClass.getMetrics().setAddedLOC(addedLOC);
            projectClass.getMetrics().setMaxAddedLOC(maxLOC);
            projectClass.getMetrics().setAvgAddedLOC(avgLOC);
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
