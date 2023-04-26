package metrics.controllers;

import metrics.models.Commit;
import metrics.models.LOCMetrics;
import metrics.models.ProjectClass;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ComputeMetrics {

    private final List<ProjectClass> allProjectClasses;
    private final List<Commit> filteredCommitsOfIssues;
    private final ExtractInfoFromGit gitExtractor;
    public ComputeMetrics(ExtractInfoFromGit gitExtractor, List<ProjectClass> allProjectClasses, List<Commit> filteredCommitsOfIssues) {
        this.allProjectClasses = allProjectClasses;
        this.filteredCommitsOfIssues = filteredCommitsOfIssues;
        this.gitExtractor = gitExtractor;
    }

    private void computeSize() {
        for(ProjectClass projectClass : allProjectClasses) {
            String[] lines = projectClass.getContentOfClass().split("\r\n|\r|\n");
            projectClass.getMetrics().setSize(lines.length);
        }
    }

    private void computeNR() {
        for(ProjectClass projectClass : allProjectClasses) {
            projectClass.getMetrics().setNumberOfRevisions(projectClass.getCommitsThatTouchTheClass().size());
        }
    }

    private void computeNfix(){
        int nFix;
        for(ProjectClass projectClass : allProjectClasses) {
            nFix = 0;
            for(Commit commitThatTouchesTheClass: projectClass.getCommitsThatTouchTheClass()) {
                if (filteredCommitsOfIssues.contains(commitThatTouchesTheClass)) {
                    nFix++;
                }
            }
            projectClass.getMetrics().setNumberOfDefectFixes(nFix);
        }
    }

    private void computeNAuth() {
        for(ProjectClass projectClass : allProjectClasses) {
            List<String> authorsOfClass = new ArrayList<>();
            for(Commit commit : projectClass.getCommitsThatTouchTheClass()) {
                RevCommit revCommit = commit.getRevCommit();
                if(!authorsOfClass.contains(revCommit.getAuthorIdent().getName())) {
                    authorsOfClass.add(revCommit.getAuthorIdent().getName());
                }
            }
            projectClass.getMetrics().setNumberOfAuthors(authorsOfClass.size());
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

            List<Integer> locAddedByClass = projectClass.getLOCAddedByClass();
            List<Integer> locRemovedByClass = projectClass.getLOCRemovedByClass();
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

            setMetrics(valAvgMaxRemovedLOC, valAvgMaxChurnLOC, valAvgMaxAddedLOC, projectClass, locAddedByClass, locRemovedByClass);
        }
    }

    private static void setMetrics(LOCMetrics valAvgMaxRemovedLOC, LOCMetrics valAvgMaxChurnLOC, LOCMetrics valAvgMaxAddedLOC, ProjectClass projectClass, List<Integer> locAddedByClass, List<Integer> locRemovedByClass) {
        if(!locAddedByClass.isEmpty()) {
            valAvgMaxAddedLOC.setAvgVal(1.0* valAvgMaxAddedLOC.getVal()/ locAddedByClass.size());
        }
        if(!locRemovedByClass.isEmpty()) {
            valAvgMaxRemovedLOC.setAvgVal(1.0* valAvgMaxRemovedLOC.getVal()/ locRemovedByClass.size());
        }
        if(!locAddedByClass.isEmpty() || !locRemovedByClass.isEmpty()) {
            valAvgMaxChurnLOC.setAvgVal(1.0* valAvgMaxChurnLOC.getVal()/ (locAddedByClass.size() + locRemovedByClass.size()));
        }

        projectClass.getMetrics().setAddedLOCMetrics(valAvgMaxAddedLOC.getVal(), valAvgMaxAddedLOC.getMaxVal(), valAvgMaxAddedLOC.getAvgVal());
        projectClass.getMetrics().setRemovedLOCMetrics(valAvgMaxRemovedLOC.getVal(), valAvgMaxRemovedLOC.getMaxVal(), valAvgMaxRemovedLOC.getAvgVal());
        projectClass.getMetrics().setChurnMetrics(valAvgMaxChurnLOC.getVal(), valAvgMaxChurnLOC.getMaxVal(), valAvgMaxChurnLOC.getAvgVal());
    }

    public void computeAllMetrics() throws IOException {
        computeSize();
        computeNR();
        computeNfix();
        computeNAuth();
        computeLOCMetrics();
    }
}
