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
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


public class ExtractMetrics {
    private ExtractMetrics(){

    }

    public static void extractDataAndElaborate(String projName, String repoURL) throws IOException, ParseException, GitAPIException {
        System.out.println(projName + " DATA EXTRACTION STARTED ==============================\n");
        ExtractInfoFromJira jiraExtractor = new ExtractInfoFromJira(projName.toUpperCase());
        List<Release> releaseList = jiraExtractor.extractAllReleases();
        List<Ticket> ticketList = jiraExtractor.extractAllTickets(releaseList);
        ExtractInfoFromGit gitExtractor = new ExtractInfoFromGit(repoURL, releaseList, ticketList);
        List<Commit> commitList = gitExtractor.extractAllCommits();
        List<Commit> filteredCommitsOfIssues = gitExtractor.filterCommitsOfIssues(commitList);
        ticketList.removeIf(ticket -> ticket.getCommitList().isEmpty());
        List<ProjectClass> projectClasses = gitExtractor.extractAllProjectClasses(commitList, ticketList);
        printExtracted(projName, releaseList, ticketList, commitList, filteredCommitsOfIssues);
        //ExtractInfoFromGit.deleteDirectory("ProjTemp");
    }

    private static void printExtracted(String projName, List<Release> releaseList, List<Ticket> ticketList, List<Commit> commitList, List<Commit> filteredCommitsOfIssues) {
        //PRINTING OUT EVERYTHING
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
        System.out.println("----------------------------------------------------------");
        System.out.println(projName + " DATA EXTRACTION FINISHED ==============================\n\n");
    }
}
