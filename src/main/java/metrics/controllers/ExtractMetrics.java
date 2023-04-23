package metrics.controllers;

import metrics.models.Commit;
import metrics.models.ProjectClass;
import metrics.models.Release;
import metrics.models.Ticket;
import metrics.utilities.CommitUtilities;
import metrics.utilities.ReleaseUtilities;
import metrics.utilities.TicketUtilities;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


public class ExtractMetrics {
    private ExtractMetrics(){

    }

    public static void extractDataAndElaborate(String projName, String repoURL) throws IOException, GitAPIException {
        //System.out.println(projName + " DATA EXTRACTION STARTED ==============================\n");
        ExtractInfoFromJira jiraExtractor = new ExtractInfoFromJira(projName.toUpperCase());
        List<Release> releaseList = jiraExtractor.extractAllReleases();
        List<Ticket> ticketList = jiraExtractor.extractAllTickets(releaseList);
        ExtractInfoFromGit gitExtractor = new ExtractInfoFromGit(projName, repoURL, releaseList, ticketList);
        List<Commit> commitList = gitExtractor.extractAllCommits();
        List<Commit> filteredCommitsOfIssues = gitExtractor.filterCommitsOfIssues(commitList);
        ticketList.removeIf(ticket -> ticket.getCommitList().isEmpty());
        List<ProjectClass> allProjectClasses = gitExtractor.extractAllProjectClasses(commitList, ticketList, releaseList.size());
        printExtracted(projName, releaseList, ticketList, commitList, filteredCommitsOfIssues);
        writeOnCsvFile(projName, releaseList, allProjectClasses);
        //ExtractInfoFromGit.deleteDirectory(projName.toLowerCase() + "Temp");
    }

    private static void writeOnCsvFile(String projName, List<Release> releaseList, List<ProjectClass> allProjectClasses) {
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(projName + "DataExtraction.csv");
            fileWriter.append("ReleaseID,File Name,Buggy").append("\n");
            for (Release release: releaseList) {
                for(ProjectClass projectClass: allProjectClasses){
                    if(projectClass.getRelease().id()==release.id()){
                        String releaseID = Integer.toString(release.id());
                        String isClassBugged = projectClass.isBugged() ? "YES" : "NO" ;
                        String className = projectClass.getName();
                        fileWriter.append(releaseID).append(",").append(className).append(",").append(isClassBugged).append("\n");
                    }
                }

            }
        } catch (Exception e) {
            System.out.println("Error in csv writer");
            e.printStackTrace();
        } finally {
            try {
                assert fileWriter != null;
                fileWriter.flush();
                fileWriter.close();
            } catch (IOException e) {
                System.out.println("Error while flushing/closing fileWriter !!!");
                e.printStackTrace();
            }
        }
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
