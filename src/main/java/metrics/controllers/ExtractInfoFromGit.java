package metrics.controllers;

import metrics.models.Commit;
import metrics.models.ProjectClass;
import metrics.models.Release;
import metrics.models.Ticket;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Pattern;

public class ExtractInfoFromGit {

    private List<Ticket> ticketList;

    private final List<Release> releaseList;
    protected final Git git;
    private final Repository repository;
    public ExtractInfoFromGit(String projName, String repoURL, List<Release> releaseList) throws IOException, GitAPIException {
        String filename = projName.toLowerCase() + "Temp";
        File directory = new File(filename);
        if(directory.exists()){
            repository = new FileRepository(filename + "\\.git");
            git = new Git(repository);
        }else{
            git = Git.cloneRepository().setURI(repoURL).setDirectory(directory).call();
            repository = git.getRepository();
        }
        this.releaseList = releaseList;
        this.ticketList = null;
    }

    public List<Ticket> getTicketList() {
        return ticketList;
    }
    public void setTicketList(List<Ticket> ticketList) {
        this.ticketList = ticketList;
    }

    public List<Release> getReleaseList() {
        return releaseList;
    }

    public List<Commit> extractAllCommits() throws IOException, GitAPIException {
        List<RevCommit> revCommitList = new ArrayList<>();
        List<Ref> branchList = git.branchList().setListMode(ListBranchCommand.ListMode.ALL).call();
        for (Ref branch : branchList) {
            Iterable<RevCommit> allRevCommits = git.log().add(repository.resolve(branch.getName())).call();
            for (RevCommit revCommit : allRevCommits) {
                if (!revCommitList.contains(revCommit)) {
                    revCommitList.add(revCommit);
                }
            }
        }
        revCommitList.sort(Comparator.comparing(o -> o.getCommitterIdent().getWhen()));
        List<Commit> commitList = new ArrayList<>();
        for (RevCommit revCommit : revCommitList) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            LocalDate commitDate = LocalDate.parse(formatter.format(revCommit.getCommitterIdent().getWhen()));
            LocalDate lowerBoundDate = LocalDate.parse(formatter.format(new Date(0)));
            for(Release release: releaseList){
                LocalDate dateOfRelease = release.releaseDate();
                if (commitDate.isAfter(lowerBoundDate) && !commitDate.isAfter(dateOfRelease)) {
                    Commit newCommit = new Commit(revCommit, release);
                    commitList.add(newCommit);
                    release.addCommit(newCommit);
                }
                lowerBoundDate = dateOfRelease;
            }
        }
        releaseList.removeIf(release -> release.getCommitList().isEmpty());
        int i = 0;
        for (Release release : releaseList) {
            release.setId(++i);
        }
        commitList.sort(Comparator.comparing(o -> o.getRevCommit().getCommitterIdent().getWhen()));
        return commitList;
    }

    public List<Commit> filterCommitsOfIssues(List<Commit> commitList) {
        List<Commit> filteredCommits = new ArrayList<>();
        for (Commit commit : commitList) {
            for (Ticket ticket : ticketList) {
                String commitFullMessage = commit.getRevCommit().getFullMessage();
                String ticketKey = ticket.getTicketKey();
                if (matchRegex(commitFullMessage, ticketKey)) {
                    filteredCommits.add(commit);
                    ticket.addCommit(commit);
                    commit.setTicket(ticket);
                }
            }
        }
        ticketList.removeIf(ticket -> ticket.getCommitList().isEmpty());
        return filteredCommits;
    }

    public static boolean matchRegex(String stringToMatch, String commitKey) {
        Pattern pattern = Pattern.compile(commitKey + "\\b");
        return pattern.matcher(stringToMatch).find();
    }

    public List<ProjectClass> extractAllProjectClasses(List<Commit> commitList, int releasesNumber) throws IOException {
        List<Commit> lastCommitList = new ArrayList<>();
        for(int i = 1; i <= releasesNumber; i++){
            List<Commit> tempCommits = new ArrayList<>(commitList);
            int finalI = i;
            tempCommits.removeIf(commit -> (commit.getRelease().id() != finalI));
            if(tempCommits.isEmpty()){
                continue;
            }
            lastCommitList.add(tempCommits.get(tempCommits.size()-1));
        }
        lastCommitList.sort(Comparator.comparing(o -> o.getRevCommit().getCommitterIdent().getWhen()));
        List<ProjectClass> allProjectClasses = new ArrayList<>();
        for(Commit lastCommit: lastCommitList){
            Map<String, String> nameAndContentOfClasses = getAllClassesNameAndContent(lastCommit.getRevCommit());
            for(Map.Entry<String, String> nameAndContentOfClass : nameAndContentOfClasses.entrySet()){
                allProjectClasses.add(new ProjectClass(nameAndContentOfClass.getKey(), nameAndContentOfClass.getValue(), lastCommit.getRelease()));
            }
        }
        completeClassesInfo(ticketList, allProjectClasses);
        keepTrackOfCommitsThatTouchTheClass(allProjectClasses, commitList);
        allProjectClasses.sort(Comparator.comparing(ProjectClass::getName));
        return allProjectClasses;
    }

    public void completeClassesInfo(List<Ticket> ticketList, List<ProjectClass> allProjectClasses) throws IOException {
        for(ProjectClass projectClass: allProjectClasses){
            projectClass.getMetrics().setBuggyness(false);
        }
        for(Ticket ticket: ticketList) {
            List<Commit> commitsContainingTicket = ticket.getCommitList();
            Release injectedVersion = ticket.getInjectedVersion();
            for (Commit commit : commitsContainingTicket) {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                RevCommit revCommit = commit.getRevCommit();
                LocalDate commitDate = LocalDate.parse(formatter.format(revCommit.getCommitterIdent().getWhen()));
                if (!commitDate.isAfter(ticket.getResolutionDate())
                        && !commitDate.isBefore(ticket.getCreationDate())) {
                    List<String> modifiedClassesNames = getTouchedClassesNames(revCommit);
                    Release releaseOfCommit = commit.getRelease();
                    for (String modifiedClass : modifiedClassesNames) {
                        labelBuggyClasses(modifiedClass, injectedVersion, releaseOfCommit, allProjectClasses);
                    }
                }
            }
        }
    }

    private void keepTrackOfCommitsThatTouchTheClass(List<ProjectClass> allProjectClasses, List<Commit> commitList) throws IOException {
        List<ProjectClass> tempProjClasses;
        for(Commit commit: commitList){
            Release release = commit.getRelease();
            tempProjClasses = new ArrayList<>(allProjectClasses);
            tempProjClasses.removeIf(tempProjClass -> !tempProjClass.getRelease().equals(release));
            List<String> modifiedClassesNames = getTouchedClassesNames(commit.getRevCommit());
            for(String modifiedClass: modifiedClassesNames){
                for(ProjectClass projectClass: tempProjClasses){
                    if(projectClass.getName().equals(modifiedClass) && !projectClass.getCommitsThatTouchTheClass().contains(commit)) {
                        projectClass.addCommitThatTouchesTheClass(commit);
                    }
                }
            }
        }
    }

    private static void labelBuggyClasses(String modifiedClass, Release injectedVersion, Release fixedVersion, List<ProjectClass> allProjectClasses) {
        for(ProjectClass projectClass: allProjectClasses){
            if(projectClass.getName().equals(modifiedClass) && projectClass.getRelease().id() < fixedVersion.id() && projectClass.getRelease().id() >= injectedVersion.id()){
                projectClass.getMetrics().setBuggyness(true);
            }
        }
    }

    private List<String> getTouchedClassesNames(RevCommit commit) throws IOException {
        List<String> touchedClassesNames = new ArrayList<>();
        try(DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE);
            ObjectReader reader = repository.newObjectReader()) {
            CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
            ObjectId newTree = commit.getTree();
            newTreeIter.reset(reader, newTree);
            RevCommit commitParent = commit.getParent(0);
            CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
            ObjectId oldTree = commitParent.getTree();
            oldTreeIter.reset(reader, oldTree);
            diffFormatter.setRepository(repository);
            List<DiffEntry> entries = diffFormatter.scan(oldTreeIter, newTreeIter);
            for(DiffEntry entry : entries) {
                if(entry.getNewPath().contains(".java") && !entry.getNewPath().contains("/test/")) {
                    touchedClassesNames.add(entry.getNewPath());
                }
            }
        } catch(ArrayIndexOutOfBoundsException ignored) {
            //ignoring when no parent is found
        }
        return touchedClassesNames;
    }

    private Map<String, String> getAllClassesNameAndContent(RevCommit revCommit) throws IOException {
        Map<String, String> allClasses = new HashMap<>();
        RevTree tree = revCommit.getTree();
        TreeWalk treeWalk = new TreeWalk(repository);
        treeWalk.addTree(tree);
        treeWalk.setRecursive(true);
        while(treeWalk.next()) {
            if(treeWalk.getPathString().contains(".java") && !treeWalk.getPathString().contains("/test/")) {
                allClasses.put(treeWalk.getPathString(), new String(repository.open(treeWalk.getObjectId(0)).getBytes(), StandardCharsets.UTF_8));
            }
        }
        treeWalk.close();
        return allClasses;
    }

    public void extractAddedOrRemovedLOC(ProjectClass projectClass) throws IOException {
        for(Commit commit : projectClass.getCommitsThatTouchTheClass()) {
            RevCommit revCommit = commit.getRevCommit();
            try(DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE)) {
                RevCommit parentComm = revCommit.getParent(0);
                diffFormatter.setRepository(repository);
                diffFormatter.setDiffComparator(RawTextComparator.DEFAULT);
                List<DiffEntry> diffEntries = diffFormatter.scan(parentComm.getTree(), revCommit.getTree());
                for(DiffEntry diffEntry : diffEntries) {
                    if(diffEntry.getNewPath().equals(projectClass.getName())) {
                        projectClass.addLOCAddedByClass(getAddedLines(diffFormatter, diffEntry));
                        projectClass.addLOCRemovedByClass(getDeletedLines(diffFormatter, diffEntry));
                    }
                }
            } catch(ArrayIndexOutOfBoundsException ignored) {
                //ignoring when no parent is found
            }
        }
    }

    private int getAddedLines(DiffFormatter diffFormatter, DiffEntry entry) throws IOException {
        int addedLines = 0;
        for(Edit edit : diffFormatter.toFileHeader(entry).toEditList()) {
            addedLines += edit.getEndB() - edit.getBeginB();
        }
        return addedLines;
    }

    private int getDeletedLines(DiffFormatter diffFormatter, DiffEntry entry) throws IOException {
        int deletedLines = 0;
        for(Edit edit : diffFormatter.toFileHeader(entry).toEditList()) {
            deletedLines += edit.getEndA() - edit.getBeginA();
        }
        return deletedLines;
    }

}
