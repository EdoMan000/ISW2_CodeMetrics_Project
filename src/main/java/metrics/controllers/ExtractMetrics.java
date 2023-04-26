package metrics.controllers;

import metrics.models.Commit;
import metrics.models.ProjectClass;
import metrics.models.Release;
import metrics.models.Ticket;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;
import java.util.List;

import static metrics.controllers.CreateCsvWithMetrics.writeOnCsvFile;
import static metrics.controllers.CreateReportFiles.writeOnReportFiles;


public class ExtractMetrics {
    private ExtractMetrics(){}

    public static void extractDataAndElaborate(String projName, String repoURL) throws IOException, GitAPIException {
        ExtractInfoFromJira jiraExtractor = new ExtractInfoFromJira(projName.toUpperCase());
        List<Release> releaseList = jiraExtractor.extractAllReleases();
        List<Ticket> ticketList = jiraExtractor.extractAllTickets(releaseList);
        ExtractInfoFromGit gitExtractor = new ExtractInfoFromGit(projName, repoURL, releaseList, ticketList);
        List<Commit> commitList = gitExtractor.extractAllCommits();
        List<Commit> filteredCommitsOfIssues = gitExtractor.filterCommitsOfIssues(commitList);
        List<ProjectClass> allProjectClasses = gitExtractor.extractAllProjectClasses(commitList, releaseList.size());
        ComputeMetrics metricsExtractor = new ComputeMetrics(gitExtractor, allProjectClasses);
        metricsExtractor.computeAllMetrics();
        List<Integer> buggyClassesPerRelease = writeOnCsvFile(projName, releaseList, allProjectClasses);
        writeOnReportFiles(projName, releaseList, gitExtractor.getTicketList(), commitList, filteredCommitsOfIssues, buggyClassesPerRelease);
        //ExtractInfoFromGit.deleteDirectory(projName.toLowerCase() + "Temp")
        //ExtractInfoFromGit.deleteDirectory("/reportFiles/")
    }
}
