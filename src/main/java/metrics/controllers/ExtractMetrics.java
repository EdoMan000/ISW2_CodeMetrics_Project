package metrics.controllers;

import metrics.models.Commit;
import metrics.models.ProjectClass;
import metrics.models.Release;
import metrics.models.Ticket;
import metrics.utilities.CommitUtilities;
import metrics.utilities.ReleaseUtilities;
import metrics.utilities.TicketUtilities;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static metrics.controllers.CreateCsvWithMetrics.writeOnCsvFile;


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
        printExtracted(projName, releaseList, gitExtractor.getTicketList(), commitList, filteredCommitsOfIssues, buggyClassesPerRelease);
        //ExtractInfoFromGit.deleteDirectory(projName.toLowerCase() + "Temp")
    }



    private static void printExtracted(String projName, List<Release> releaseList, List<Ticket> ticketList, List<Commit> commitList, List<Commit> filteredCommitsOfIssues, List<Integer> buggyClassesPerRelease) {
        //PRINTING OUT EVERYTHING
        System.out.println("\n" + projName + " DATA EXTRACTION STARTED ==============================\n\n");
        System.out.println("\n---------- Release List ---------");
        for (Release release : releaseList) {
            ReleaseUtilities.printRelease(release);
        }
        System.out.println("\n---------- Ticket List ---------");
        List<Ticket> ticketOrderedByCreation = new ArrayList<>(ticketList);
        ticketOrderedByCreation.sort(Comparator.comparing(Ticket::getCreationDate));
        for (Ticket ticket : ticketOrderedByCreation) {
            TicketUtilities.printTicket(ticket);
        }
        System.out.println("\n---------- Git Data Extraction ---------");
        for (Commit commit: commitList){
            CommitUtilities.printCommit(commit);
        }
        System.out.println("----------------------------------------------------------");
        System.out.println("EXTRACTION INFO:\n"
                + releaseList.size() + " RELEASES \n"
                + ticketList.size() + " TICKETS \n"
                + commitList.size() + " TOTAL COMMITS \n"
                + filteredCommitsOfIssues.size() + " COMMITS CONTAINING BUG-ISSUES");
        System.out.println("----------------------------------------------------------\n");
        for (Release release : releaseList) {
            System.out.println("RELEASE " + release.releaseName() + " HAS " + buggyClassesPerRelease.get(release.id()-1) + " BUGGY CLASSES\n");
        }
        System.out.println(projName + " DATA EXTRACTION FINISHED ==============================\n");
    }
}
