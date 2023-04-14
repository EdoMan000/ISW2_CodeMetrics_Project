package metrics.controllers;

import metrics.models.Release;
import metrics.models.Ticket;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

public class ExtractMetrics {
    private ExtractMetrics(){

    }

    public static void extractDataAndElaborate(String projName) throws IOException, ParseException {
        System.out.println(projName + " DATA EXTRACTION STARTED ==============================\n");
        ExtractInfoFromJira jiraExtractor = new ExtractInfoFromJira(projName);
        System.out.println("---------- Release List ---------");
        List<Release> releaseList = jiraExtractor.extractAllReleases();
        System.out.println("\n---------- Ticket List ---------");
        List<Ticket> ticketList = jiraExtractor.extractAllTickets(releaseList);
        System.out.println("\n---------- Git Data Extraction ---------");

        System.out.println(projName + " DATA EXTRACTION FINISHED ==============================\n\n");
    }
}
