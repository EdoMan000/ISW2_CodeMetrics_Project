package metrics.controllers;

import metrics.models.Release;
import metrics.models.Ticket;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Repository;

import java.io.IOException;
import java.util.List;

public class ExtractInfoFromGit {
    private List<Ticket> ticketList;
    private List<Release> releaseList;
    private Git git;
    private Repository repository;
    public ExtractInfoFromGit(String pathOfRepo, List<Release> releaseList, List<Ticket> ticketList) throws IOException {
        this.repository = new FileRepository(pathOfRepo);
        this.releaseList = releaseList;
        this.ticketList = ticketList;
    }

}
