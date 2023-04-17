package metrics.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Ticket {
    private final String ticketKey;

    private final Date creationDate;
    private final Date resolutionDate;
    private Release injectedVersion;
    private final Release openingVersion;
    private final Release fixedVersion;
    private List<Release> affectedVersions;
    private List<Commit> commitList;

    public Ticket(String ticketKey, Date creationDate, Date resolutionDate, Release openingVersion, Release fixedVersion, List<Release> affectedVersions) {
        this.ticketKey = ticketKey;
        this.creationDate = creationDate;
        this.resolutionDate = resolutionDate;
        if(affectedVersions.isEmpty()){
            this.injectedVersion = null;
        }else{
            this.injectedVersion = affectedVersions.get(0);
        }
        this.openingVersion = openingVersion;
        this.fixedVersion = fixedVersion;
        this.affectedVersions = affectedVersions;
        this.commitList = new ArrayList<>();
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

    public Date getCreationDate() {
        return creationDate;
    }

    public void addCommit(Commit newCommit) {
        if(!commitList.contains(newCommit)){
            this.commitList.add(newCommit);
        }
    }

    public List<Commit> getCommitList(){
        return commitList;
    }

    public Date getResolutionDate() {
        return resolutionDate;
    }
}
