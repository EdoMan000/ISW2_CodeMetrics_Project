package metrics.utilities;

import metrics.models.Commit;
import org.eclipse.jgit.revwalk.RevCommit;

import java.text.SimpleDateFormat;

public class CommitUtilities {

    public static void printCommit(Commit commit) {
        RevCommit revCommit = commit.getRevCommit();
        System.out.println("Commit[ID= " + revCommit.getName()
                //+ ", committer= " + revCommit.getCommitterIdent().getName()
                //+ ", shortMessage= " + revCommit.getShortMessage()
                + ", ticket= " + commit.getTicket().getTicketKey()
                + ", release= " + commit.getRelease().releaseName()
                + ", creationDate= " + (new SimpleDateFormat("yyyy-MM-dd").format(revCommit.getCommitterIdent().getWhen()))
                + "]\n"
        );
    }
}
