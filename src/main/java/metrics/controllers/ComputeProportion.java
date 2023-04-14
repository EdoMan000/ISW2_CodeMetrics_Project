package metrics.controllers;

import metrics.models.Release;
import metrics.models.Ticket;
import metrics.utilities.TicketUtilities;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


public class ComputeProportion {

    public static final int TRESHOLD_FOR_COLDSTART = 50;

    private enum OtherProjects {
        AVRO,
        STORM,
        SYNCOPE,
        OPENJPA,
        ZOOKEEPER
    }
    private static float standardProportionComputation(List<Ticket> filteredTicketsList) {
        filteredTicketsList.sort(Comparator.comparing(Ticket::getCreationDate));
        System.out.println("PROPORTION -----------------------------------------------");
        //PROPORTION = (FV-IV)/(FV-OV)
        float totalProportion = 0.0F;
        float propForTicket;
        for (Ticket correctTicket : filteredTicketsList) {
            TicketUtilities.printTicket(correctTicket);
            propForTicket = (float) (correctTicket.getFixedVersion().id() - correctTicket.getInjectedVersion().id())
                    /
                    (correctTicket.getFixedVersion().id()-correctTicket.getOpeningVersion().id());
            totalProportion+=propForTicket;
        }
        System.out.println("TICKETSLIST.SIZE() FILTERED FOR PROPORTION: " + filteredTicketsList.size());
        System.out.println("PROPORTION: " + totalProportion/filteredTicketsList.size());
        System.out.println("----------------------------------------------------------");
        return totalProportion/filteredTicketsList.size();
    }


    private static float coldStartProportionComputation() throws IOException, ParseException {
        System.out.println("COLD_START_PROPORTION COMPUTATION STARTED ===================");
        float totalProportion = 0.0F;
        int validComputations = 0;
        for(OtherProjects projName: OtherProjects.values()){
            ExtractInfoFromJira jiraExtractor = new ExtractInfoFromJira(projName.toString());
            List<Release> releaseList = jiraExtractor.extractAllReleases();
            List<Ticket> ticketCompleteList = jiraExtractor.getTickets(releaseList);
            List<Ticket> ticketCorrectList = TicketUtilities.returnCorrectTickets(ticketCompleteList);
            List<Ticket> ticketFilteredList = TicketUtilities.filterTicketsForProportion(ticketCorrectList);
            if(ticketFilteredList.size() >= TRESHOLD_FOR_COLDSTART){
                totalProportion += ComputeProportion.standardProportionComputation(ticketFilteredList);
                validComputations++;
            }
            System.out.println("----------------------------------------------------------");
        }
        float proportion = totalProportion/validComputations;
        System.out.println("TOTAL PROPORTION ON ALL PROJECTS FOR COLD START: " + proportion);
        System.out.println("COLD_START_PROPORTION COMPUTATION ENDED ===================");
        System.out.println("----------------------------------------------------------");
        return proportion;
    }

    public static float computeProportion(List<Ticket> filteredTickets) throws IOException, ParseException {
        float proportion;
        if(filteredTickets.size() >= TRESHOLD_FOR_COLDSTART){
            proportion = ComputeProportion.standardProportionComputation(filteredTickets);
        }
        else{
            proportion = ComputeProportion.coldStartProportionComputation();
        }
        return proportion;
    }
}
