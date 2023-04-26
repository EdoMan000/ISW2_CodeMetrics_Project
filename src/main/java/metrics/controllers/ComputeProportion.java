package metrics.controllers;

import metrics.models.Release;
import metrics.models.Ticket;
import metrics.utilities.TicketUtilities;
import metrics.utilities.FileWriterUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
    private static int coldStartValueRetrievedCount = 0;

    public static final int THRESHOLD_FOR_COLD_START = 5;

    private ComputeProportion() {}

    private enum OtherProjects {
        AVRO,
        SYNCOPE,
        STORM,
        TAJO,
        ZOOKEEPER
    }
    private static float incrementalProportionComputation(List<Ticket> filteredTicketsList) throws IOException {
        filteredTicketsList.sort(Comparator.comparing(Ticket::getResolutionDate));
        outputToFile.append("PROPORTION -----------------------------------------------\n");
        // PROPORTION = (FV-IV)/(FV-OV)
        float totalProportion = 0.0F;
        for (Ticket correctTicket : filteredTicketsList) {
            float propForTicket = ((float) correctTicket.getFixedVersion().id() - (float) correctTicket.getInjectedVersion().id())
                    /
                    ((float) correctTicket.getFixedVersion().id() - (float) correctTicket.getOpeningVersion().id());
            totalProportion+=propForTicket;
        }
        outputToFile.append("#TICKETS FILTERED FOR INCREMENTAL PROPORTION: ").append(filteredTicketsList.size()).append("\n");
        float average = totalProportion / filteredTicketsList.size();
        outputToFile.append("PROPORTION AVERAGE: ").append(average).append("\n")
                .append("----------------------------------------------------------\n");
        return average;
    }


    private static float coldStartProportionComputation() throws IOException {
        if(coldStartComputedProportion != null){
            coldStartValueRetrievedCount++;
            outputToFile.append("[").append(coldStartValueRetrievedCount)
                    .append("] COLD-START VALUE RETRIEVED\n");
            return coldStartComputedProportion;
        }
        outputToFile.append("COLD-START PROPORTION COMPUTATION STARTED ===================\n\n");
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
        outputToFile.append("PROPORTION LIST: ").append(proportionList).append("\n");
        Collections.sort(proportionList);
        float median;
        int size = proportionList.size();
        if (size % 2 == 0) {
            median = (proportionList.get((size / 2) - 1) + proportionList.get(size / 2)) / 2;
        } else {
            median = proportionList.get(size / 2);
        }
        outputToFile.append("MEDIAN PROPORTION OUT OF ALL PROJECTS FOR COLD START: ").append(median).append("\n")
                .append("COLD-START PROPORTION COMPUTATION ENDED ===================\n\n");
        coldStartComputedProportion = median;
        coldStartValueRetrievedCount++;
        return median;
    }

    public static float computeProportion(List<Ticket> fixedTicketsList, String projName) {
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
            FileWriter fileWriter = new FileWriter(file);
            if (fixedTicketsList.size() >= THRESHOLD_FOR_COLD_START) {
                proportion = ComputeProportion.incrementalProportionComputation(fixedTicketsList);
            } else {
                proportion = ComputeProportion.coldStartProportionComputation();
            }
            fileWriter.append(outputToFile.toString());
            FileWriterUtils.flushAndCloseFW(fileWriter, logger, NAME_OF_THIS_CLASS);
        } catch(IOException e){
            logger.info("Error in ComputeProportion when trying to create directory");
        }
        return proportion;
    }

}
