package metrics.utilities;

import metrics.controllers.ComputeProportion;
import metrics.models.Release;
import metrics.models.Ticket;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static java.lang.Math.max;

public class TicketUtilities {


    private TicketUtilities() {
    }

    public static List<Ticket> fixTicketList(List<Ticket> ticketsList, List<Release> releasesList, String projName) throws URISyntaxException {
        List<Ticket> ticketsForProportionList = new ArrayList<>();
        List<Ticket> finalTicketsList = new ArrayList<>();
        float proportion;
        for(Ticket ticket : ticketsList){
            if(!isCorrectTicket(ticket)){
                proportion = ComputeProportion.computeProportion(ticketsForProportionList, projName, ticket, true);
                fixTicketWithProportion(ticket, releasesList, proportion);
                completeAffectedVersionsList(ticket, releasesList);
            }else{
                ComputeProportion.computeProportion(ticketsForProportionList, projName, ticket, false);
                completeAffectedVersionsList(ticket, releasesList);
                ticketsForProportionList.add(ticket);
            }
            finalTicketsList.add(ticket);
        }
        finalTicketsList.sort(Comparator.comparing(Ticket::getResolutionDate));
        return finalTicketsList;
    }

    public static List<Ticket> returnCorrectTickets(List<Ticket> ticketsList){
        List<Ticket> correctTickets = new ArrayList<>();
        for (Ticket ticket : ticketsList) {
            if (isCorrectTicket(ticket)) {
                correctTickets.add(ticket);
            }
        }
        correctTickets.sort(Comparator.comparing(Ticket::getResolutionDate));
        return correctTickets;
    }

    private static void completeAffectedVersionsList(Ticket ticket, List<Release> releasesList) {
        List<Release> completeAffectedVersionsList = new ArrayList<>();
        for(int i = ticket.getInjectedVersion().id(); i < ticket.getFixedVersion().id(); i++){
            for(Release release : releasesList){
                if(release.id() == i){
                    completeAffectedVersionsList.add(new Release(release.id(), release.releaseName(), release.releaseDate()));
                    break;
                }
            }
        }
        completeAffectedVersionsList.sort(Comparator.comparing(Release::releaseDate));
        ticket.setAffectedVersions(completeAffectedVersionsList);
    }

    private static void fixTicketWithProportion(Ticket ticket, List<Release> releasesList, float proportion) {
        List<Release> affectedVersionsList = new ArrayList<>();
        int injectedVersionId;
        //IV = max(1; FV-(FV-OV)*P)
        if(ticket.getFixedVersion().id() == ticket.getOpeningVersion().id()){
            injectedVersionId = max(1, (int) (ticket.getFixedVersion().id()-proportion));
        }else{
            injectedVersionId = max(1, (int) (ticket.getFixedVersion().id()-((ticket.getFixedVersion().id()-ticket.getOpeningVersion().id())*proportion)));
        }
        for (Release release : releasesList){
            if(release.id() == injectedVersionId){
                affectedVersionsList.add(new Release(release.id(), release.releaseName(), release.releaseDate()));
                break;
            }
        }
        affectedVersionsList.sort(Comparator.comparing(Release::releaseDate));
        ticket.setAffectedVersions(affectedVersionsList);
        ticket.setInjectedVersion(affectedVersionsList.get(0));
    }

    private static boolean isCorrectTicket(Ticket ticket) {
        return !ticket.getAffectedVersions().isEmpty();
    }


}
