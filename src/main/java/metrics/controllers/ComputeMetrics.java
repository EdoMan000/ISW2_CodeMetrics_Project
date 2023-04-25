package metrics.controllers;

import metrics.models.LOCMetrics;
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
        LOCMetrics valAvgMaxRemovedLOC = new LOCMetrics();
        LOCMetrics valAvgMaxChurnLOC = new LOCMetrics();
        LOCMetrics valAvgMaxAddedLOC = new LOCMetrics();
        int i;
        for(ProjectClass projectClass : allProjectClasses) {
            valAvgMaxAddedLOC.setVal(0);valAvgMaxAddedLOC.setAvgVal(0);valAvgMaxAddedLOC.setMaxVal(0);
            valAvgMaxRemovedLOC.setVal(0);valAvgMaxRemovedLOC.setAvgVal(0);valAvgMaxRemovedLOC.setMaxVal(0);
            valAvgMaxChurnLOC.setVal(0);valAvgMaxChurnLOC.setAvgVal(0);valAvgMaxChurnLOC.setMaxVal(0);
            gitExtractor.extractAddedOrRemovedLOC(projectClass);

            List<Integer> locAddedByClass = projectClass.getlOCAddedByClass();
            List<Integer> locRemovedByClass = projectClass.getlOCRemovedByClass();
            for(i = 0; i < locAddedByClass.size(); i++) {
                int addedLineOfCode = locAddedByClass.get(i);
                int removedLineOfCode = locRemovedByClass.get(i);
                int churningFactor = Math.abs(locAddedByClass.get(i) - locRemovedByClass.get(i));
                valAvgMaxAddedLOC.addToVal(addedLineOfCode);
                valAvgMaxRemovedLOC.addToVal(removedLineOfCode);
                valAvgMaxChurnLOC.addToVal(churningFactor);
                if(addedLineOfCode > valAvgMaxAddedLOC.getMaxVal()) {
                    valAvgMaxAddedLOC.setMaxVal(addedLineOfCode);
                }
                if(removedLineOfCode > valAvgMaxRemovedLOC.getMaxVal()) {
                    valAvgMaxRemovedLOC.setMaxVal(removedLineOfCode);
                }
                if(churningFactor > valAvgMaxChurnLOC.getMaxVal()) {
                    valAvgMaxChurnLOC.setMaxVal(churningFactor);
                }
            }

            if(!locAddedByClass.isEmpty()) {
                valAvgMaxAddedLOC.setAvgVal(1.0*valAvgMaxAddedLOC.getVal()/ locAddedByClass.size());
            }
            if(!locRemovedByClass.isEmpty()) {
                valAvgMaxRemovedLOC.setAvgVal(1.0*valAvgMaxRemovedLOC.getVal()/ locRemovedByClass.size());
            }
            if(!locAddedByClass.isEmpty() || !locRemovedByClass.isEmpty()) {
                valAvgMaxChurnLOC.setAvgVal(1.0*valAvgMaxChurnLOC.getVal()/ (locAddedByClass.size() + locRemovedByClass.size()));
            }

            projectClass.getMetrics().setAddedLOCMetrics(valAvgMaxAddedLOC.getVal(), valAvgMaxAddedLOC.getMaxVal(), valAvgMaxAddedLOC.getAvgVal());
            projectClass.getMetrics().setRemovedLOCMetrics(valAvgMaxRemovedLOC.getVal(), valAvgMaxRemovedLOC.getMaxVal(), valAvgMaxRemovedLOC.getAvgVal());
            projectClass.getMetrics().setChurnMetrics(valAvgMaxChurnLOC.getVal(), valAvgMaxChurnLOC.getMaxVal(), valAvgMaxChurnLOC.getAvgVal());
        }
    }

    public void computeAllMetrics() throws IOException {
        computeSize();
        computeLOCMetrics();
    }
}
