package metrics.utilities;

import metrics.controllers.ComputeProportion;
import metrics.models.Release;
import metrics.models.Ticket;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.max;

public class TicketUtils {
    public static List<Ticket> fixTicketList(List<Ticket> ticketsList, List<Release> releasesList) {
        //if there is no AV -> there is no IV -> need to compute Proportion
        // verify IV <= OV <= FV
        List<Ticket> CorrectTickets = new ArrayList<>();
        List<Ticket> IncorrectTickets = new ArrayList<>();
        List<Ticket> fixedTicketsList = new ArrayList<>();
        for (Ticket ticket : ticketsList) {
            if(isCorrectTicket(ticket)){
                CorrectTickets.add(ticket);
            }else{
                IncorrectTickets.add(ticket);
            }
        }
        //PROPORTION =========================
        float proportion = ComputeProportion.fromCorrectTicketsList(CorrectTickets);
        /*if(CorrectTickets.size() >= 10){

        }
        else{
            //TODO COLD START
        }*/
        //====================================
        for (Ticket ticket : IncorrectTickets) {
            fixTicketWithProportion(ticket, releasesList, proportion);
        }
        fixedTicketsList.addAll(CorrectTickets);
        fixedTicketsList.addAll(IncorrectTickets);
        for (Ticket ticket : fixedTicketsList) {
            completeAffectedVersionsList(ticket, releasesList);
        }
        return fixedTicketsList;
    }

    private static void completeAffectedVersionsList(Ticket ticket, List<Release> releasesList) {
        List<Release> completeAffectedVersionsList = new ArrayList<>();
        if(ticket.getInjectedVersion().id() == ticket.getFixedVersion().id()){
            completeAffectedVersionsList.add(ticket.getInjectedVersion());
            ticket.setAffectedVersions(completeAffectedVersionsList);
            return;
        }
        for(int i = ticket.getInjectedVersion().id(); i < ticket.getFixedVersion().id(); i++){
            for(Release release : releasesList){
                if(release.id() == i){
                    completeAffectedVersionsList.add(new Release(release.id(), release.releaseName(), release.releaseDate()));
                    break;
                }
            }
        }
        ticket.setAffectedVersions(completeAffectedVersionsList);
    }

    private static void fixTicketWithProportion(Ticket ticket, List<Release> releasesList, float proportion) {
        List<Release> affectedVersionsList = new ArrayList<>();
        //IV = max(1; FV-(FV-OV)*P)
        int injectedVersionId = max(1, (int) (ticket.getFixedVersion().id()-((ticket.getFixedVersion().id()-ticket.getOpeningVersion().id())*proportion)));
        for (Release release : releasesList){
            if(release.id() == injectedVersionId){
                affectedVersionsList.add(new Release(release.id(), release.releaseName(), release.releaseDate()));
                break;
            }
        }
        ticket.setAffectedVersions(affectedVersionsList);
        ticket.setInjectedVersion(affectedVersionsList.get(0));
    }

    public static void printTicket(Ticket ticket) {
        List<String> IDs = new ArrayList<>();
        for(Release release : ticket.getAffectedVersions()) {
            IDs.add(release.releaseName());
        }
        System.out.println("Ticket[key=" + ticket.getTicketKey()
                + ", injectedVersion=" + ticket.getInjectedVersion().releaseName()
                + ", openingVersion=" + ticket.getOpeningVersion().releaseName()
                + ", fixedVersion=" + ticket.getFixedVersion().releaseName()
                + ", affectedVersions=" + IDs
                + "]"
        );
    }

    private static boolean isCorrectTicket(Ticket ticket) {
        return !ticket.getAffectedVersions().isEmpty() &&
                ticket.getInjectedVersion().id() < ticket.getOpeningVersion().id() &&
                ticket.getOpeningVersion().id() <= ticket.getFixedVersion().id();
    }
}
