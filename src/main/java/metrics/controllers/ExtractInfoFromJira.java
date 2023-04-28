package metrics.controllers;

import metrics.models.Release;
import metrics.models.Ticket;
import metrics.utilities.JsonUtilities;
import metrics.utilities.ReleaseUtilities;
import metrics.utilities.TicketUtilities;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ExtractInfoFromJira {

    private final String projName;

    public ExtractInfoFromJira(String projName) {
        this.projName = projName.toUpperCase();
    }

    public List<Release> extractAllReleases() throws IOException, JSONException, URISyntaxException {
        //Fills the arraylist with releases dates and orders them
        //Ignores releases with missing dates
        List<Release> releaseList = new ArrayList<>();
        int i=0;
        String url = "https://issues.apache.org/jira/rest/api/latest/project/" + this.projName;
        JSONObject json = JsonUtilities.readJsonFromUrl(url);
        JSONArray versions = json.getJSONArray("versions");
        for (; i < versions.length(); i++) {
            String releaseName;
            String releaseDate;
            if (versions.getJSONObject(i).has("releaseDate") && versions.getJSONObject(i).has("name")) {
                releaseDate = versions.getJSONObject(i).get("releaseDate").toString();
                releaseName = versions.getJSONObject(i).get("name").toString();
                releaseList.add(new Release(releaseName, LocalDate.parse(releaseDate)));
            }
        }
        releaseList.sort(Comparator.comparing(Release::releaseDate));
        i = 0;
        for (Release release : releaseList) {
            release.setId(++i);
        }
        return releaseList;
    }

    public List<Ticket> extractAllTickets(List<Release> releasesList) throws IOException, JSONException, URISyntaxException {
        List<Ticket> ticketsList = getTickets(releasesList);
        List<Ticket> fixedTicketsList;
        fixedTicketsList = TicketUtilities.fixTicketList(ticketsList, releasesList, projName);
        fixedTicketsList.sort(Comparator.comparing(Ticket::getResolutionDate));
        return fixedTicketsList;
    }

    public List<Ticket> getTickets(List<Release> releasesList) throws IOException, URISyntaxException {
        int j;
        int i = 0;
        int total;
        List<Ticket> ticketsList = new ArrayList<>();
        //Get JSON API for closed bugs w/ AV in the project
        do {
            //Only gets a max of 1000 at a time, so must do this multiple times if bugs >1000
            j = i + 1000;
            String url = "https://issues.apache.org/jira/rest/api/2/search?jql=project=%22"
                    + this.projName + "%22AND%22issueType%22=%22Bug%22AND" +
                    "(%22status%22=%22Closed%22OR%22status%22=%22Resolved%22)" +
                    "AND%22resolution%22=%22Fixed%22&fields=key,versions,created,resolutiondate&startAt="
                    + i + "&maxResults=" + j;
            JSONObject json = JsonUtilities.readJsonFromUrl(url);
            JSONArray issues = json.getJSONArray("issues");
            total = json.getInt("total");
            for (; i < total && i < j; i++) {
                //Iterate through each bug
                String key = issues.getJSONObject(i%1000).get("key").toString();
                JSONObject fields = issues.getJSONObject(i%1000).getJSONObject("fields");
                String creationDateString = fields.get("created").toString();
                String resolutionDateString = fields.get("resolutiondate").toString();
                LocalDate creationDate = LocalDate.parse(creationDateString.substring(0,10));
                LocalDate resolutionDate = LocalDate.parse(resolutionDateString.substring(0,10));
                JSONArray affectedVersionsArray = fields.getJSONArray("versions");
                Release openingVersion = ReleaseUtilities.getReleaseAfterOrEqualDate(creationDate, releasesList);
                Release fixedVersion =  ReleaseUtilities.getReleaseAfterOrEqualDate(resolutionDate, releasesList);
                List<Release> affectedVersionsList = ReleaseUtilities.returnValidAffectedVersions(affectedVersionsArray, releasesList);
                if(!affectedVersionsList.isEmpty()
                        && openingVersion!=null
                        && fixedVersion!=null
                        && (!affectedVersionsList.get(0).releaseDate().isBefore(openingVersion.releaseDate())
                                        || openingVersion.releaseDate().isAfter(fixedVersion.releaseDate()))){
                    continue;
                }
                if(openingVersion != null && fixedVersion != null && openingVersion.id()!=releasesList.get(0).id()){
                    ticketsList.add(new Ticket(key, creationDate, resolutionDate, openingVersion, fixedVersion, affectedVersionsList));
                }
            }
        } while (i < total);
        ticketsList.sort(Comparator.comparing(Ticket::getResolutionDate));
        return ticketsList;
    }
}
