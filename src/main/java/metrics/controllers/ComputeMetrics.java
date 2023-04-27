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
        LOCMetrics removedLOC = new LOCMetrics();
        LOCMetrics churnLOC = new LOCMetrics();
        LOCMetrics addedLOC = new LOCMetrics();
        int i;
        for(ProjectClass projectClass : allProjectClasses) {
            addedLOC.setVal(0);addedLOC.setAvgVal(0);addedLOC.setMaxVal(0);
            removedLOC.setVal(0);removedLOC.setAvgVal(0);removedLOC.setMaxVal(0);
            churnLOC.setVal(0);churnLOC.setAvgVal(0);churnLOC.setMaxVal(0);
            gitExtractor.extractAddedOrRemovedLOC(projectClass);

            List<Integer> locAddedByClass = projectClass.getLOCAddedByClass();
            List<Integer> locRemovedByClass = projectClass.getLOCRemovedByClass();
            for(i = 0; i < locAddedByClass.size(); i++) {
                int addedLineOfCode = locAddedByClass.get(i);
                int removedLineOfCode = locRemovedByClass.get(i);
                int churningFactor = Math.abs(locAddedByClass.get(i) - locRemovedByClass.get(i));
                addedLOC.addToVal(addedLineOfCode);
                removedLOC.addToVal(removedLineOfCode);
                churnLOC.addToVal(churningFactor);
                if(addedLineOfCode > addedLOC.getMaxVal()) {
                    addedLOC.setMaxVal(addedLineOfCode);
                }
                if(removedLineOfCode > removedLOC.getMaxVal()) {
                    removedLOC.setMaxVal(removedLineOfCode);
                }
                if(churningFactor > churnLOC.getMaxVal()) {
                    churnLOC.setMaxVal(churningFactor);
                }
            }

            setMetrics(removedLOC, churnLOC, addedLOC, projectClass, locAddedByClass, locRemovedByClass);
        }
    }

    private static void setMetrics(LOCMetrics removedLOC, LOCMetrics churn, LOCMetrics addedLOC, ProjectClass projectClass, List<Integer> locAddedByClass, List<Integer> locRemovedByClass) {
        int nRevisions = projectClass.getMetrics().getNumberOfRevisions();
        if(!locAddedByClass.isEmpty()) {
            addedLOC.setAvgVal(1.0* addedLOC.getVal()/ nRevisions);
        }
        if(!locRemovedByClass.isEmpty()) {
            removedLOC.setAvgVal(1.0* removedLOC.getVal()/ nRevisions);
        }
        if(!locAddedByClass.isEmpty() || !locRemovedByClass.isEmpty()) {
            churn.setAvgVal(1.0* churn.getVal()/ nRevisions);
        }
        projectClass.getMetrics().setAddedLOCMetrics(addedLOC.getVal(), addedLOC.getMaxVal(), addedLOC.getAvgVal());
        projectClass.getMetrics().setRemovedLOCMetrics(removedLOC.getVal(), removedLOC.getMaxVal(), removedLOC.getAvgVal());
        projectClass.getMetrics().setChurnMetrics(churn.getVal(), churn.getMaxVal(), churn.getAvgVal());
    }

    public void computeAllMetrics() throws IOException {
        computeSize();
        computeNR();
        computeNfix();
        computeNAuth();
        computeLOCMetrics();
    }
}
