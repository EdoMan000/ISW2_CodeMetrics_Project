package metrics.controllers;

import metrics.models.Release;
import metrics.models.Ticket;
import metrics.utilities.FileWriterUtils;
import metrics.utilities.TicketUtilities;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;


public class ComputeProportion {

    public static final String NAME_OF_THIS_CLASS = ComputeProportion.class.getName();
    private static final Logger logger = Logger.getLogger(NAME_OF_THIS_CLASS);
    private static final StringBuilder outputToFile = new StringBuilder();
    private static Float coldStartComputedProportion = null;

    public static final int THRESHOLD_FOR_COLD_START = 5;

    private ComputeProportion() {}

    private enum OtherProjects {
        AVRO,
        SYNCOPE,
        STORM,
        TAJO,
        ZOOKEEPER
    }
    private static float incrementalProportionComputation(List<Ticket> filteredTicketsList, Ticket ticket, boolean writeInfo, boolean doActualComputation) {
        if (writeUsedOrNot(ticket, doActualComputation)) return 0;
        filteredTicketsList.sort(Comparator.comparing(Ticket::getResolutionDate));
        outputToFile.append("\n[*]PROPORTION[*]-----------------------------------------------\n");
        if (writeInfo) {
            outputToFile.append("----------------------\n[").append(ticket.getTicketKey()).append("]\n----------------------\n");
        }
        // PROPORTION = (FV-IV)/(FV-OV)
        float totalProportion = 0.0F;
        float denominator;
        for (Ticket correctTicket : filteredTicketsList) {
            if (correctTicket.getFixedVersion().id() != correctTicket.getOpeningVersion().id()) {
                denominator = ((float) correctTicket.getFixedVersion().id() - (float) correctTicket.getOpeningVersion().id());
            }else{
                denominator = 1;
            }
            float propForTicket = ((float) correctTicket.getFixedVersion().id() - (float) correctTicket.getInjectedVersion().id())
                    / denominator;
            totalProportion+=propForTicket;
        }
        outputToFile.append("SIZE_OF_FILTERED_TICKET_LIST: ").append(filteredTicketsList.size()).append("\n");
        float average = totalProportion / filteredTicketsList.size();
        outputToFile.append("PROPORTION AVERAGE: ").append(average).append("\n")
                .append("----------------------------------------------------------\n");
        return average;
    }

    private static boolean writeUsedOrNot(Ticket ticket, boolean doActualComputation) {
        if(!doActualComputation){
            if (ticket.getFixedVersion().id() != ticket.getOpeningVersion().id()) {
                outputToFile.append("\n----------------------\n[").append(ticket.getTicketKey()).append("]\n----------------------\n").append("PROPORTION: WILL USE FOR PROPORTION AS IT IS!").append("\n----------------------\n");
            }else{
                outputToFile.append("\n----------------------\n[").append(ticket.getTicketKey()).append("]\n----------------------\n").append("PROPORTION: WILL SET DENOMINATOR=1!").append("\n----------------------\n");
            }
            return true;
        }
        return false;
    }


    private static float coldStartProportionComputation(Ticket ticket, boolean doActualComputation) throws IOException, URISyntaxException {
        if (writeUsedOrNot(ticket, doActualComputation)) return 0;
        if(coldStartComputedProportion != null){
            outputToFile.append("\n[*]COLD-START RETRIEVED[*]---------------------------------------\n");
            outputToFile.append("----------------------\n[").append(ticket.getTicketKey()).append("]\n----------------------\n").append("PROPORTION: ").append(coldStartComputedProportion).append("\n----------------------\n");
            return coldStartComputedProportion;
        }
        outputToFile.append("\n\nCOLD-START PROPORTION COMPUTATION STARTED ===================\n");
        outputToFile.append("----------------------\n[").append(ticket.getTicketKey()).append("]\n----------------------\n");
        List<Float> proportionList = new ArrayList<>();
        for(OtherProjects projName: OtherProjects.values()){
            ExtractInfoFromJira jiraExtractor = new ExtractInfoFromJira(projName.toString());
            List<Release> releaseList = jiraExtractor.extractAllReleases();
            List<Ticket> ticketCompleteList = jiraExtractor.getTickets(releaseList);
            List<Ticket> ticketCorrectList = TicketUtilities.returnCorrectTickets(ticketCompleteList);
            if(ticketCorrectList.size() >= THRESHOLD_FOR_COLD_START){
                proportionList.add(ComputeProportion.incrementalProportionComputation(ticketCorrectList, ticket, false, doActualComputation));
            }
        }
        Collections.sort(proportionList);
        outputToFile.append("\nPROPORTION LIST: ").append(" -----------------------------------------------\n")
                .append(proportionList).append("\n");
        float median;
        int size = proportionList.size();
        if (size % 2 == 0) {
            median = (proportionList.get((size / 2) - 1) + proportionList.get(size / 2)) / 2;
        } else {
            median = proportionList.get(size / 2);
        }
        outputToFile.append("MEDIAN PROPORTION OUT OF ALL PROJECTS FOR COLD START: ").append(median).append("\n")
                .append("-----------------------------------------------------------------\n\n\n")
                .append("COLD-START PROPORTION COMPUTATION ENDED ===================\n\n");
        coldStartComputedProportion = median;
        return median;
    }

    public static float computeProportion(List<Ticket> fixedTicketsList, String projName, Ticket ticket, boolean doActualComputation) throws URISyntaxException {
        float proportion = 0;
        try {
            File file = new File("outputFiles/reportFiles/" + projName);
            if (!file.exists()) {
                boolean created = file.mkdirs();
                if (!created) {
                    throw new IOException();
                }
            }
            file = new File("outputFiles/reportFiles/" + projName + "/Proportion.txt");
            try(FileWriter fileWriter = new FileWriter(file)) {
                if (fixedTicketsList.size() >= THRESHOLD_FOR_COLD_START) {
                    proportion = ComputeProportion.incrementalProportionComputation(fixedTicketsList, ticket, true, doActualComputation);
                } else {
                    proportion = ComputeProportion.coldStartProportionComputation(ticket, doActualComputation);
                }
                fileWriter.append(outputToFile.toString());
                FileWriterUtils.flushAndCloseFW(fileWriter, logger, NAME_OF_THIS_CLASS);
            }
        } catch(IOException e){
            logger.info("Error in ComputeProportion when trying to create directory");
        }
        return proportion;
    }

}
