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
    public static final boolean INCREMENTAL_PROPORTION = true;
    public static final boolean COLD_START_PROPORTION = false;
    public static Float coldStartComputedProportion = null;

    public static final int THRESHOLD_FOR_COLD_START = 10;

    private enum OtherProjects {
        AVRO,
        STORM,
        SYNCOPE,
        TAJO,
        ZOOKEEPER
    }
    private static float incrementalProportionComputation(List<Ticket> filteredTicketsList, boolean isIncremental) {
        filteredTicketsList.sort(Comparator.comparing(Ticket::getResolutionDate));
        System.out.println("PROPORTION -----------------------------------------------");
        // PROPORTION = (FV-IV)/(FV-OV)
        List<Float> proportionList = new ArrayList<>();
        float totalProportion = 0.0F;

        for (Ticket correctTicket : filteredTicketsList) {
            float propForTicket = ((float) correctTicket.getFixedVersion().id() - (float) correctTicket.getInjectedVersion().id())
                    /
                    ((float) correctTicket.getFixedVersion().id() - (float) correctTicket.getOpeningVersion().id());
            if (isIncremental) {
                totalProportion+=propForTicket;
            }
            proportionList.add(propForTicket);
        }
        System.out.println("TICKETS-LIST.SIZE() FILTERED FOR PROPORTION: " + filteredTicketsList.size());
        System.out.println("PROPORTION LIST: " + proportionList);
        Collections.sort(proportionList);
        if (isIncremental) {
            float average = totalProportion/proportionList.size();
            System.out.println("PROPORTION AVERAGE: " + average);
            System.out.println("----------------------------------------------------------");
            return average;
        }else{
            float median;
            int size = proportionList.size();
            if (size % 2 == 0) {
                median = (proportionList.get((size / 2) - 1) + proportionList.get(size / 2)) / 2;
            } else {
                median = proportionList.get(size / 2);
            }
            System.out.println("PROPORTION MEDIAN: " + median);
            System.out.println("----------------------------------------------------------");
            return median;
        }
    }


    private static float coldStartProportionComputation() throws IOException, ParseException {
        if(coldStartComputedProportion != null){
            return coldStartComputedProportion;
        }
        System.out.println("COLD_START_PROPORTION COMPUTATION STARTED ===================");
        float totalProportion = 0.0F;
        int validComputations = 0;
        for(OtherProjects projName: OtherProjects.values()){
            ExtractInfoFromJira jiraExtractor = new ExtractInfoFromJira(projName.toString());
            List<Release> releaseList = jiraExtractor.extractAllReleases();
            List<Ticket> ticketCompleteList = jiraExtractor.getTickets(releaseList);
            List<Ticket> ticketCorrectList = TicketUtilities.returnCorrectTickets(ticketCompleteList);
            List<Ticket> ticketFilteredList = TicketUtilities.filterTicketsForProportion(ticketCorrectList);
            if(ticketFilteredList.size() >= THRESHOLD_FOR_COLD_START){
                totalProportion += ComputeProportion.incrementalProportionComputation(ticketFilteredList, COLD_START_PROPORTION);
                validComputations++;
            }
            System.out.println("----------------------------------------------------------");
        }
        float proportion = totalProportion/validComputations;
        System.out.println("TOTAL PROPORTION ON ALL PROJECTS FOR COLD START: " + proportion);
        System.out.println("COLD_START_PROPORTION COMPUTATION ENDED ===================");
        System.out.println("----------------------------------------------------------");
        coldStartComputedProportion = proportion;
        return proportion;
    }

    public static float computeProportion(List<Ticket> fixedTicketsList) throws IOException, ParseException {
        if(fixedTicketsList.size() >= THRESHOLD_FOR_COLD_START){
            return ComputeProportion.incrementalProportionComputation(fixedTicketsList, INCREMENTAL_PROPORTION);
        }
        else{
            return ComputeProportion.coldStartProportionComputation();
        }
    }
}
