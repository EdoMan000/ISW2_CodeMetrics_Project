package metrics.utilities;

import metrics.models.Commit;
import metrics.models.Release;
import metrics.models.Ticket;
import org.eclipse.jgit.revwalk.RevCommit;

import java.text.SimpleDateFormat;
import java.time.LocalDate;

public class CommitUtilities {

    private CommitUtilities() {
    }

    public static void printCommit(Commit commit) {
        RevCommit revCommit = commit.getRevCommit();
        Ticket ticket = commit.getTicket();
        Release release = commit.getRelease();
        System.out.println("Commit[ID= " + revCommit.getName()
                //+ ", committer= " + revCommit.getCommitterIdent().getName()
                //+ ", message= " + revCommit.getFullMessage()
                + ((ticket == null) ? "": ", ticket= " + commit.getTicket().getTicketKey())
                + ", release= " + release.releaseName()
                + ", creationDate= " + LocalDate.parse((new SimpleDateFormat("yyyy-MM-dd").format(revCommit.getCommitterIdent().getWhen())))
                + "]\n"
        );
    }
}
