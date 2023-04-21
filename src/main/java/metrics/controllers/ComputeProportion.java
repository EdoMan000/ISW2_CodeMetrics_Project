package metrics.controllers;

import metrics.models.Release;
import metrics.models.Ticket;
import metrics.utilities.TicketUtilities;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class ComputeProportion {
    public static Float coldStartComputedProportion = null;
    public static int coldStartValueRetrievedCount = 0;

    public static final int THRESHOLD_FOR_COLD_START = 5;

    private enum OtherProjects {
        AVRO,
        SYNCOPE,
        STORM,
        TAJO,
        ZOOKEEPER
    }
    private static float incrementalProportionComputation(List<Ticket> filteredTicketsList) {
        filteredTicketsList.sort(Comparator.comparing(Ticket::getResolutionDate));
        System.out.println("PROPORTION -----------------------------------------------");
        // PROPORTION = (FV-IV)/(FV-OV)
        float totalProportion = 0.0F;
        for (Ticket correctTicket : filteredTicketsList) {
            float propForTicket = ((float) correctTicket.getFixedVersion().id() - (float) correctTicket.getInjectedVersion().id())
                    /
                    ((float) correctTicket.getFixedVersion().id() - (float) correctTicket.getOpeningVersion().id());
            totalProportion+=propForTicket;
        }
        System.out.println("#TICKETS FILTERED FOR INCREMENTAL PROPORTION: " + filteredTicketsList.size());
        float average = totalProportion/filteredTicketsList.size();
        System.out.println("PROPORTION AVERAGE: " + average);
        System.out.println("----------------------------------------------------------");
        return average;
    }


    private static float coldStartProportionComputation() throws IOException, ParseException {
        if(coldStartComputedProportion != null){
            coldStartValueRetrievedCount++;
            System.out.println("[" + coldStartValueRetrievedCount+ "] COLD-START VALUE RETRIEVED");
            return coldStartComputedProportion;
        }
        System.out.println("COLD-START PROPORTION COMPUTATION STARTED ===================");
        List<Float> proportionList = new ArrayList<>();
        for(OtherProjects projName: OtherProjects.values()){
            ExtractInfoFromJira jiraExtractor = new ExtractInfoFromJira(projName.toString());
            List<Release> releaseList = jiraExtractor.extractAllReleases();
            List<Ticket> ticketCompleteList = jiraExtractor.getTickets(releaseList);
            List<Ticket> ticketCorrectList = TicketUtilities.returnCorrectTickets(ticketCompleteList);
            List<Ticket> ticketFilteredList = TicketUtilities.filterTicketsForProportion(ticketCorrectList);
            if(ticketFilteredList.size() >= THRESHOLD_FOR_COLD_START){
                proportionList.add(ComputeProportion.incrementalProportionComputation(ticketFilteredList));
            }
        }

        System.out.println("PROPORTION LIST: " + proportionList);
        Collections.sort(proportionList);
        float median;
        int size = proportionList.size();
        if (size % 2 == 0) {
            median = (proportionList.get((size / 2) - 1) + proportionList.get(size / 2)) / 2;
        } else {
            median = proportionList.get(size / 2);
        }
        System.out.println("MEDIAN PROPORTION OUT OF ALL PROJECTS FOR COLD START: " + median);
        System.out.println("COLD-START PROPORTION COMPUTATION ENDED ===================");
        System.out.println("----------------------------------------------------------");
        coldStartComputedProportion = median;
        coldStartValueRetrievedCount++;
        return median;
    }

    public static float computeProportion(List<Ticket> fixedTicketsList) throws IOException, ParseException {
        if(fixedTicketsList.size() >= THRESHOLD_FOR_COLD_START){
            return ComputeProportion.incrementalProportionComputation(fixedTicketsList);
        }
        else{
            return ComputeProportion.coldStartProportionComputation();
        }
    }
}
