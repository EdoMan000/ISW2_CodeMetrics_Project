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
    private final List<Ticket> ticketList;
    private final List<Release> releaseList;
    private final Git git;
    private final Repository repository;
    public ExtractInfoFromGit(String projName, String repoURL, List<Release> releaseList, List<Ticket> ticketList) throws IOException, GitAPIException {
        String filename = projName.toLowerCase() + "Temp";
        File directory = new File(filename);
        if(directory.exists()){
            this.repository = new FileRepository(filename + "\\.git");
            this.git = new Git(this.repository);
        }else{
            this.git = Git.cloneRepository().setURI(repoURL).setDirectory(directory).call();
            this.repository = git.getRepository();
        }
        this.releaseList = releaseList;
        this.ticketList = ticketList;
    }

    public static void deleteDirectory(String directoryPath) {
        File directory = new File(directoryPath);
        if(directory.isDirectory()){
            File[] contents = directory.listFiles();
            if(contents!=null){
                for(File content : contents){
                    deleteDirectory(content.getAbsolutePath());
                }
            }
        }
        directory.delete();
    }

    public List<Commit> extractAllCommits() throws IOException, GitAPIException {
        List<RevCommit> revCommitList = new ArrayList<>();
        List<Ref> branchList = this.git.branchList().setListMode(ListBranchCommand.ListMode.ALL).call();
        for (Ref branch : branchList) {
            Iterable<RevCommit> commitsList = this.git.log().add(this.repository.resolve(branch.getName())).call();
            for (RevCommit revCommit : commitsList) {
                if (!revCommitList.contains(revCommit)) {
                    revCommitList.add(revCommit);
                }
            }
        }
        revCommitList.sort(Comparator.comparing(o -> o.getCommitterIdent().getWhen()));
        List<Commit> commitList = new ArrayList<>();
        for (RevCommit revCommit : revCommitList) {
            LocalDate commitDate = LocalDate.parse(new SimpleDateFormat("yyyy-MM-dd").format(revCommit.getCommitterIdent().getWhen()));
            LocalDate lowerBoundDate = LocalDate.parse("2000-01-01");
            for(Release release: this.releaseList){
                //lowerBoundDate < commitDate <= releaseDate
                if (commitDate.isAfter(lowerBoundDate) && !commitDate.isAfter(release.releaseDate())) {
                    Commit newCommit = new Commit(revCommit, release);
                    commitList.add(newCommit);
                    release.addCommit(newCommit);
                }
                lowerBoundDate = release.releaseDate();
            }

        }
        commitList.sort(Comparator.comparing(o -> o.getRevCommit().getCommitterIdent().getWhen()));
        return commitList;
    }

    public List<Commit> filterCommitsOfIssues(List<Commit> commitList) {
        List<Commit> filteredCommits = new ArrayList<>();
        for (Commit commit : commitList) {
            for (Ticket ticket : this.ticketList) {
                String commitFullMessage = commit.getRevCommit().getFullMessage();
                String ticketKey = ticket.getTicketKey();
                if (matchRegex(commitFullMessage, ticketKey)) {
                    filteredCommits.add(commit);
                    ticket.addCommit(commit);
                    commit.setTicket(ticket);
                }
            }
        }
        return filteredCommits;
    }

    public static boolean matchRegex(String stringToMatch, String commitKey) {
        Pattern pattern = Pattern.compile(commitKey + "\\b");
        return pattern.matcher(stringToMatch).find();
    }

    public List<ProjectClass> extractAllProjectClasses(List<Commit> commitList, int releasesNumber) throws IOException {

        List<Commit> lastCommits = new ArrayList<>();
        for(int i=1; i<=releasesNumber; i++){
            List<Commit> tempCommits = new ArrayList<>(commitList);
            int finalI = i;
            tempCommits.removeIf(commit -> !(commit.getRelease().id() == finalI));
            if(tempCommits.isEmpty()){
                continue;
            }
            lastCommits.add(tempCommits.get(tempCommits.size()-1));
        }
        lastCommits.sort(Comparator.comparing(o -> o.getRevCommit().getCommitterIdent().getWhen()));
        List<ProjectClass> allProjectClasses = new ArrayList<>();
        for(Commit commit: lastCommits){
            Map<String, String> nameAndContentOfClasses = getAllClassesNameAndContent(commit.getRevCommit());
            for(Map.Entry<String, String> nameAndContentOfClass : nameAndContentOfClasses.entrySet()){
                allProjectClasses.add(new ProjectClass(nameAndContentOfClass.getKey(), nameAndContentOfClass.getValue(), commit.getRelease()));
            }
        }
        for(Ticket ticket: ticketList){
            completeClassesInfo(ticket, allProjectClasses);
        }
        keepTrackOfCommitsThatModify(allProjectClasses, commitList);
        return allProjectClasses;
    }

    private void completeClassesInfo(Ticket ticket, List<ProjectClass> allProjectClasses) throws IOException {
        List<Commit> commitsContainingTicket = ticket.getCommitList();
        Release injectedVersion = ticket.getInjectedVersion();
        for(Commit commit: commitsContainingTicket){
            if(!commit.getRelease().releaseDate().isAfter(ticket.getFixedVersion().releaseDate())){
                //We assume as TRUE the Jira info about resolutionDATE (ticket FV is correct)
                // -> the fact that the commit with older date contains ticketID is considered an error
                // -> class must not be labeled as buggy
                List<String> modifiedClassesNames = getModifiedClassesNames(commit.getRevCommit());
                Release release = commit.getRelease();
                for(String modifiedClass: modifiedClassesNames){
                    labelBuggyClasses(modifiedClass, injectedVersion, release, allProjectClasses);
                }
            }
        }
    }

    private void keepTrackOfCommitsThatModify(List<ProjectClass> allProjectClasses, List<Commit> commitList) throws IOException {
        for(Commit commit: commitList){
            Release release = commit.getRelease();
            List<String> modifiedClassesNames = getModifiedClassesNames(commit.getRevCommit());
            for(String modifiedClass: modifiedClassesNames){
                for(ProjectClass projectClass: allProjectClasses){
                    if(projectClass.getName().equals(modifiedClass) && projectClass.getRelease().id() == release.id() && !projectClass.getCommitsThatModify().contains(commit)) {
                        List<Commit> commitsThatModify = projectClass.getCommitsThatModify();
                        commitsThatModify.add(commit);
                        projectClass.setCommitsThatModify(commitsThatModify);
                    }
                }
            }
        }
    }

    private void labelBuggyClasses(String modifiedClass, Release injectedVersion, Release fixedVersion, List<ProjectClass> allProjectClasses) {
        for(ProjectClass projectClass: allProjectClasses){
            if(projectClass.getName().equals(modifiedClass) && projectClass.getRelease().id() < fixedVersion.id() && projectClass.getRelease().id() >= injectedVersion.id()){
                projectClass.setBugged(true);
            }
        }
    }

    private List<String> getModifiedClassesNames(RevCommit commit) throws IOException {
        List<String> modifiedClassesNames = new ArrayList<>();
        try(DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE);
            ObjectReader reader = this.repository.newObjectReader()) {
            CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
            ObjectId newTree = commit.getTree();
            newTreeIter.reset(reader, newTree);
            RevCommit commitParent = commit.getParent(0);
            CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
            ObjectId oldTree = commitParent.getTree();
            oldTreeIter.reset(reader, oldTree);
            diffFormatter.setRepository(this.repository);
            List<DiffEntry> entries = diffFormatter.scan(oldTreeIter, newTreeIter);
            for(DiffEntry entry : entries) {
                if(entry.getChangeType().equals(DiffEntry.ChangeType.MODIFY) && entry.getNewPath().contains(".java") && !entry.getNewPath().contains("/test/")) {
                    modifiedClassesNames.add(entry.getNewPath());
                }
            }
        } catch(ArrayIndexOutOfBoundsException ignored) {

        }
        return modifiedClassesNames;
    }

    private Map<String, String> getAllClassesNameAndContent(RevCommit revCommit) throws IOException {
        Map<String, String> allClasses = new HashMap<>();
        RevTree tree = revCommit.getTree();
        TreeWalk treeWalk = new TreeWalk(this.repository);
        treeWalk.addTree(tree);
        treeWalk.setRecursive(true);
        while(treeWalk.next()) {
            if(treeWalk.getPathString().contains(".java") && !treeWalk.getPathString().contains("/test/")) {
                allClasses.put(treeWalk.getPathString(), new String(this.repository.open(treeWalk.getObjectId(0)).getBytes(), StandardCharsets.UTF_8));
            }
        }
        treeWalk.close();
        return allClasses;
    }

}
