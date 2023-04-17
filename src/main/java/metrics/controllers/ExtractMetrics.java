package metrics.controllers;

import metrics.models.Commit;
import metrics.models.Release;
import metrics.models.Ticket;
import metrics.utilities.CommitUtilities;
import metrics.utilities.ReleaseUtilities;
import metrics.utilities.TicketUtilities;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

public class ExtractMetrics {
    private ExtractMetrics(){

    }

    public static void extractDataAndElaborate(String projName) throws IOException, ParseException, GitAPIException {
        System.out.println(projName + " DATA EXTRACTION STARTED ==============================\n");
        ExtractInfoFromJira jiraExtractor = new ExtractInfoFromJira(projName.toUpperCase());
        List<Release> releaseList = jiraExtractor.extractAllReleases();
        List<Ticket> ticketList = jiraExtractor.extractAllTickets(releaseList);
        ExtractInfoFromGit gitExtractor = new ExtractInfoFromGit("C:\\Users\\EdoMan000\\Downloads\\UNIVERSITY\\ISW2\\" +
            projName.toLowerCase() + "\\.git", releaseList, ticketList);
        List<Commit> commitList = gitExtractor.extractAllCommits();
        //REMOVING TICKETS WITHOUT FIX-COMMITS
        ticketList.removeIf(ticket -> ticket.getCommitList().isEmpty());
        //PRINTING OUT EVERYTHING
        System.out.println("\n---------- Release List ---------");
        for (Release release : releaseList) {
            ReleaseUtilities.printRelease(release);
        }
        System.out.println("\n---------- Ticket List ---------");
        for (Ticket ticket : ticketList) {
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
                + commitList.size() + " COMMITS "        );
        System.out.println("----------------------------------------------------------");
        System.out.println(projName + " DATA EXTRACTION FINISHED ==============================\n\n");
    }
}
