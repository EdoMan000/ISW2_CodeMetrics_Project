package metrics.models;

import java.util.List;

public class Ticket {
    private final String ticketKey;
    private Release injectedVersion;
    private Release openingVersion;
    private Release fixedVersion;
    private List<Release> affectedVersions;

    public Ticket(String ticketKey, Release openingVersion, Release fixedVersion, List<Release> affectedVersions) {
        this.ticketKey = ticketKey;
        if(affectedVersions.isEmpty()){
            this.injectedVersion = null;
        }else{
            this.injectedVersion = affectedVersions.get(0);
        }
        this.openingVersion = openingVersion;
        this.fixedVersion = fixedVersion;
        this.affectedVersions = affectedVersions;
    }


    public Release getInjectedVersion() {
        return injectedVersion;
    }

    public void setInjectedVersion(Release injectedVersion) {
        this.injectedVersion = injectedVersion;
    }

    public Release getOpeningVersion() {
        return openingVersion;
    }

    public Release getFixedVersion() {
        return fixedVersion;
    }

    public List<Release> getAffectedVersions() {
        return affectedVersions;
    }

    public void setAffectedVersions(List<Release> affectedVersions) {
        this.affectedVersions = affectedVersions;
    }

    public String getTicketKey() {
        return ticketKey;
    }
}
