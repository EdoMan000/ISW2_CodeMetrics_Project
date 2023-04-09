package metrics.models;

import java.util.List;

public class Ticket {
    private final String ticketKey;
    private Release injectedVersion;
    private final Release openingVersion;
    private final Release fixedVersion;
    private final List<Release> affectedVersions;

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

    public List<Release> getAffectedVersions() {
        return affectedVersions;
    }

    public Release getFixedVersion() {
        return fixedVersion;
    }

    public Release getOpeningVersion() {
        return openingVersion;
    }

    public Release getInjectedVersion() {
        return injectedVersion;
    }

    public void setInjectedVersion(Release injectedVersion) {
        this.injectedVersion = injectedVersion;
    }

    public String getTicketKey() {
        return ticketKey;
    }
}
