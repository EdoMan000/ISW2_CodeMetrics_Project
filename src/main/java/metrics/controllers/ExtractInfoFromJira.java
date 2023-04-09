package metrics.controllers;

import metrics.models.Release;
import metrics.models.Ticket;
import metrics.utilities.JSONReader;
import metrics.utilities.ReleaseUtilities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ExtractInfoFromJira {

    private String projName;

    public ExtractInfoFromJira(String projName) {
        this.projName = projName.toUpperCase();
    }

    public List<Release> extractAllReleases() throws IOException, JSONException, ParseException {
        //Fills the arraylist with releases dates and orders them
        //Ignores releases with missing dates
        Map<Date, String> releaseMap = new TreeMap<>();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        int i=0;
        String url = "https://issues.apache.org/jira/rest/api/latest/project/" + this.projName + "/version";
        JSONObject json = JSONReader.readJsonFromUrl(url);
        JSONArray versions = json.getJSONArray("values");
        for (; i < versions.length(); i++) {
            String releaseName = "";
            String releaseDate = "";
            if (versions.getJSONObject(i).has("releaseDate") && versions.getJSONObject(i).has("name")) {
                releaseDate = versions.getJSONObject(i).get("releaseDate").toString();
                releaseName = versions.getJSONObject(i).get("name").toString();
                releaseMap.put(format.parse(releaseDate), releaseName);
            }
        }
        return createReleasesList(releaseMap);
    }

    private List<Release> createReleasesList(Map<Date, String> releaseMap) {
        List<Release> releases = new ArrayList<>();
        int i=1;
        for(Map.Entry<Date, String> release : releaseMap.entrySet()) {
            releases.add(new Release(i, release.getValue(), release.getKey()));
            i++;
        }
        for (Release element : releases) {
            System.out.println(element);
        }
        return releases;

    }

    public List<Ticket> extractAllTickets(List<Release> releasesList) throws IOException, JSONException, ParseException {
        int j = 0, i = 0, total = 1;
        List<Ticket> ticketsList = new ArrayList<>();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        //Get JSON API for closed bugs w/ AV in the project
        do {
            //Only gets a max of 1000 at a time, so must do this multiple times if bugs >1000
            j = i + 1000;
            String url = "https://issues.apache.org/jira/rest/api/2/search?jql=project=%22"
                    + this.projName + "%22AND%22issueType%22=%22Bug%22AND" +
                    "(%22status%22=%22Closed%22OR%22status%22=%22Resolved%22)" +
                    "AND%22resolution%22=%22Fixed%22&fields=key,versions,created,resolutiondate&startAt="
                    + i + "&maxResults=" + j;
            JSONObject json = JSONReader.readJsonFromUrl(url);
            JSONArray issues = json.getJSONArray("issues");
            total = json.getInt("total");
            for (; i < total && i < j; i++) {
                //Iterate through each bug
                String key = issues.getJSONObject(i%1000).get("key").toString();
                JSONObject fields = issues.getJSONObject(i%1000).getJSONObject("fields");
                String creationDate = fields.get("created").toString();
                String resolutionDate = fields.get("resolutiondate").toString();
                JSONArray affectedVersionsArray = fields.getJSONArray("versions");
                Release openingVersion = ReleaseUtilities.getReleaseAfterDate(format.parse(creationDate), releasesList);
                Release fixedVersion =  ReleaseUtilities.getReleaseAfterDate(format.parse(resolutionDate), releasesList);
                List<Release> affectedVersionsList = ReleaseUtilities.returnValidAffectedVersions(affectedVersionsArray, releasesList);
                if(openingVersion != null && fixedVersion != null){
                    ticketsList.add(new Ticket(key, openingVersion, fixedVersion, affectedVersionsList));
                }
            }
        } while (i < total);
        int ticketnum = 1;
        for (Ticket element : ticketsList) {
            StringBuilder sb = new StringBuilder();
            boolean firstIteration = true;
            for (Release affectedVersion : element.getAffectedVersions()){
                if(firstIteration){
                    sb.append(affectedVersion.releaseName());
                    firstIteration = false;
                }else{
                    sb.append(", ").append(affectedVersion.releaseName());
                }
            }
            String aVString, iVString;
            if(sb.isEmpty()){
                aVString = "NO AFFECTED VERSIONS";
                iVString = "NO INJECTED VERSION";
            }else{
                aVString = "affectedVersions=" + sb.toString();
                iVString = "injectedVersion=" + element.getInjectedVersion().releaseName();
            }
            System.out.println("Ticket[key=" + element.getTicketKey()
                    + ", " + iVString
                    + ", openingVersion=" + element.getOpeningVersion().releaseName()
                    + ", fixedVersion=" + element.getFixedVersion().releaseName()
                    + ", " + aVString
                    + "]"
            );
            ticketnum++;
        }
        System.out.println("----------------------------------------------------------");
        System.out.println("TOTAL OF " + ticketnum + " TICKETS AND " + releasesList.size() + " RELEASES FOUND");
        System.out.println("----------------------------------------------------------");
        return ticketsList;
    }

}
