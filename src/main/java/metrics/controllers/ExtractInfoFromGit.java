package metrics.controllers;

import metrics.models.Commit;
import metrics.models.Release;
import metrics.models.Ticket;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class ExtractInfoFromGit {
    private List<Ticket> ticketList;
    private List<Release> releaseList;
    private Git git;
    private Repository repository;
    public ExtractInfoFromGit(String pathOfRepo, List<Release> releaseList, List<Ticket> ticketList) throws IOException {
        this.repository = new FileRepository(pathOfRepo);
        this.git = new Git(this.repository);
        this.releaseList = releaseList;
        this.ticketList = ticketList;
    }

    public List<Commit> extractAllCommits() throws IOException, GitAPIException, ParseException {
        List<RevCommit> revCommitList = new ArrayList<>();
        List<Ref> branchList = this.git.branchList().setListMode(ListBranchCommand.ListMode.ALL).call();
        for (Ref branch : branchList) {
            Iterable<RevCommit> commitsList = this.git.log().add(this.repository.resolve(branch.getName())).call();
            for (RevCommit commit : commitsList) {
                if (!revCommitList.contains(commit)) {
                    revCommitList.add(commit);
                }
            }
        }
        revCommitList.sort(Comparator.comparing(o -> o.getCommitterIdent().getWhen()));
        List<Commit> commitList = new ArrayList<>();
        for (RevCommit commit : revCommitList) {
            for (Ticket ticket : this.ticketList) {
                Date commitDate = commit.getCommitterIdent().getWhen();
                Date lowerBoundDate = new SimpleDateFormat("yyyy-MM-dd").parse("2000-01-01");
                for(Release release: releaseList){
                    //if lowerBoundDate < commitDate <= releaseDate then the commit has been done in that release
                    if (commit.getFullMessage().contains(ticket.getTicketKey())
                            && commitDate.after(lowerBoundDate)
                            && !(commitDate.after(release.releaseDate()))) {
                        Commit newCommit = new Commit(commit, ticket, release);
                        commitList.add(newCommit);
                        ticket.addCommit(newCommit);
                        release.addCommit(newCommit);
                    }
                    lowerBoundDate = release.releaseDate();
                }
            }
        }
        commitList.sort(Comparator.comparing(o -> o.getRevCommit().getCommitterIdent().getWhen()));
        return commitList;
    }

}
