package metrics.utilities;

import metrics.controllers.ComputeProportion;
import metrics.models.Release;
import metrics.models.Ticket;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static java.lang.Math.max;

public class TicketUtilities {

    public static List<Ticket> fixTicketList(List<Ticket> ticketsList, List<Release> releasesList) throws IOException, ParseException {
        //if there is no AV -> there is no IV -> need to compute Proportion
        // verify IV <= OV <= FV
        List<Ticket> fixedTicketsList = new ArrayList<>();
        float proportion;
        for(Ticket ticket : ticketsList){
            if(!isCorrectTicket(ticket)){
                proportion = ComputeProportion.computeProportion(filterTicketsForProportion(fixedTicketsList));
                fixTicketWithProportion(ticket, releasesList, proportion);
            }
            completeAffectedVersionsList(ticket, releasesList);
            fixedTicketsList.add(ticket);
        }
        fixedTicketsList.sort(Comparator.comparing(Ticket::getResolutionDate));
        return fixedTicketsList;
    }

    public static List<Ticket> returnCorrectTickets(List<Ticket> ticketsList){
        List<Ticket> CorrectTickets = new ArrayList<>();
        for (Ticket ticket : ticketsList) {
            if (isCorrectTicket(ticket)) {
                CorrectTickets.add(ticket);
            }
        }
        CorrectTickets.sort(Comparator.comparing(Ticket::getResolutionDate));
        return CorrectTickets;
    }

    //we filter out tickets that would make denominator zero in proportion computation
    public static List<Ticket> filterTicketsForProportion(List<Ticket> ticketCompleteList) {
        List<Ticket> CorrectTickets = new ArrayList<>();
        for (Ticket ticket : ticketCompleteList) {
            if (ticket.getFixedVersion().id() != ticket.getOpeningVersion().id()) {
                CorrectTickets.add(ticket);
            }
        }
        CorrectTickets.sort(Comparator.comparing(Ticket::getResolutionDate));
        return CorrectTickets;
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

    public static void printTicket(Ticket ticket) {
        List<String> IDs = new ArrayList<>();
        for(Release release : ticket.getAffectedVersions()) {
            IDs.add(release.releaseName());
        }
        System.out.println("Ticket[key= " + ticket.getTicketKey()
                + ", injectedVersion= " + ticket.getInjectedVersion().releaseName()
                + ", openingVersion= " + ticket.getOpeningVersion().releaseName()
                + ", fixedVersion= " + ticket.getFixedVersion().releaseName()
                + ", affectedVersions= " + IDs
                //+ ", numOfCommits= " + ticket.getCommitList().size()
                + ", creationDate= " + (new SimpleDateFormat("yyyy-MM-dd").format(ticket.getCreationDate()))
                + ", resolutionDate= " + (new SimpleDateFormat("yyyy-MM-dd").format(ticket.getResolutionDate()))
                + "]\n"
        );
    }

    private static boolean isCorrectTicket(Ticket ticket) {
        return !ticket.getAffectedVersions().isEmpty() &&
                ticket.getOpeningVersion() != null &&
                ticket.getFixedVersion() != null &&
                ticket.getInjectedVersion().id() < ticket.getOpeningVersion().id() &&
                ticket.getOpeningVersion().id() <= ticket.getFixedVersion().id();
    }


}
