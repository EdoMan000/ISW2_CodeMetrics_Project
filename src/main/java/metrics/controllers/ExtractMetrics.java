package metrics.controllers;

import metrics.models.Commit;
import metrics.models.ProjectClass;
import metrics.models.Release;
import metrics.models.Ticket;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.logging.Logger;

import static metrics.controllers.CreateCsvWithMetrics.writeOnCsvFile;
import static metrics.controllers.CreateReportFiles.writeOnReportFiles;


public class ExtractMetrics {
    private static final Logger logger = Logger.getLogger(ExtractMetrics.class.getName());
    private ExtractMetrics(){}

    public static void extractDataAndElaborate(String projName, String repoURL) throws IOException, GitAPIException, URISyntaxException {
        logger.info(projName + " DATA EXTRACTION STARTED...\n\n");
        ExtractInfoFromJira jiraExtractor = new ExtractInfoFromJira(projName.toUpperCase());
        List<Release> releaseList = jiraExtractor.extractAllReleases();
        logger.info(projName + " RELEASES EXTRACTED - [*OK*]\n\n");
        List<Ticket> ticketList = jiraExtractor.extractAllTickets(releaseList);
        logger.info(projName + " TICKETS EXTRACTED - [*OK*]\n\n");
        ExtractInfoFromGit gitExtractor = new ExtractInfoFromGit(projName, repoURL, releaseList, ticketList);
        List<Commit> commitList = gitExtractor.extractAllCommits();
        List<Commit> filteredCommitsOfIssues = gitExtractor.filterCommitsOfIssues(commitList);
        logger.info(projName + " COMMITS EXTRACTED - [*OK*]\n\n");
        List<ProjectClass> allProjectClasses = gitExtractor.extractAllProjectClasses(commitList, releaseList.size());
        logger.info(projName + " CLASSES EXTRACTED - [*OK*]\n\n");
        ComputeMetrics metricsExtractor = new ComputeMetrics(gitExtractor, allProjectClasses, filteredCommitsOfIssues);
        metricsExtractor.computeAllMetrics();
        logger.info(projName + " METRICS COMPUTED - [*OK*]\n\n");
        List<Integer> buggyClassesPerRelease = writeOnCsvFile(projName, releaseList, allProjectClasses);
        writeOnReportFiles(projName, releaseList, gitExtractor.getTicketList(), commitList, filteredCommitsOfIssues, buggyClassesPerRelease);
        logger.info(projName + " FILE CREATION AND WRITING DONE - [*OK*]\n\n");
        //ExtractInfoFromGit.deleteDirectory(projName.toLowerCase() + "Temp")
        //ExtractInfoFromGit.deleteDirectory("/reportFiles/")
    }
}
