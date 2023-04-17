package metrics.models;

import org.eclipse.jgit.revwalk.RevCommit;

public final class Commit {
    private RevCommit revCommit;
    private Ticket ticket;

    private Release release;

    public Commit(RevCommit revCommit, Ticket ticket, Release release) {
        this.revCommit = revCommit;
        this.ticket = ticket;
        this.release = release;
    }

    public RevCommit getRevCommit() {
        return revCommit;
    }

    public void setRevCommit(RevCommit revCommit) {
        this.revCommit = revCommit;
    }

    public Ticket getTicket() {
        return ticket;
    }

    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
    }

    public Release getRelease() {
        return release;
    }

    public void setRelease(Release release) {
        this.release = release;
    }
}
