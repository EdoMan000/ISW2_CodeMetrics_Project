package metrics.controllers;

import metrics.models.Release;
import metrics.models.Ticket;
import metrics.utilities.TicketUtils;

import java.util.ArrayList;
import java.util.List;

public class ComputeProportion {
    public static float fromCorrectTicketsList(List<Ticket> correctTicketsList) {
        //PROPORTION = (FV-IV)/(FV-OV)
        float totalProportion = 0.0F;
        float propForTicket;
        int validComputations = 0;
        for (Ticket correctTicket : correctTicketsList) {
            if(correctTicket.getFixedVersion().id() != correctTicket.getOpeningVersion().id()){
                propForTicket = (float) (correctTicket.getFixedVersion().id() - correctTicket.getInjectedVersion().id())
                        /
                        (correctTicket.getFixedVersion().id()-correctTicket.getOpeningVersion().id());
                totalProportion+=propForTicket;
                validComputations++;
            }
        }
        return totalProportion/validComputations;
    }


}
