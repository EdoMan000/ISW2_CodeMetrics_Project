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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExtractInfoFromGit {
    private final List<Ticket> ticketList;
    private final List<Release> releaseList;
    private final Git git;
    private final Repository repository;
    public ExtractInfoFromGit(String repoURL, List<Release> releaseList, List<Ticket> ticketList) throws IOException, GitAPIException {
        File directory = new File("ProjTemp");
        if(directory.exists()){
            this.repository = new FileRepository("ProjTemp\\.git");
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
        for (RevCommit revCommit : revCommitList) {
            Date commitDate = revCommit.getCommitterIdent().getWhen();
            Date lowerBoundDate = new SimpleDateFormat("yyyy-MM-dd").parse("2000-01-01");
            for(Release release: this.releaseList){
                //if lowerBoundDate < commitDate <= releaseDate then the revCommit has been done in that release
                if (commitDate.after(lowerBoundDate) && !commitDate.after(release.releaseDate())) {
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

    public List<Commit> filterCommitsOfIssues(List<Commit> commitList) throws ParseException {
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
        Pattern pattern = Pattern.compile(commitKey + "+[^0-9]");
        Matcher matcher = pattern.matcher(stringToMatch);
        return matcher.find();
    }

    public List<ProjectClass> extractAllProjectClasses(List<Commit> commitList, List<Ticket> ticketList) throws IOException {
        List<ProjectClass> allProjectClasses = new ArrayList<>();
        for(Commit commit: commitList){
            Map<String, String> nameAndContentOfClasses = getAllClassesNameAndContent(commit.getRevCommit());
            for(Map.Entry<String, String> nameAndContentOfClass : nameAndContentOfClasses.entrySet()){
                allProjectClasses.add(new ProjectClass(nameAndContentOfClass.getKey(), nameAndContentOfClass.getValue(), commit.getRelease()));
            }
        }
        for(Ticket ticket: ticketList){
            completeClassesInfo(ticket, allProjectClasses);
        }
        setCommitsThatModify(allProjectClasses, commitList);
        return allProjectClasses;
    }

    private void setCommitsThatModify(List<ProjectClass> allProjectClasses, List<Commit> commitList) throws IOException {
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

    private void completeClassesInfo(Ticket ticket, List<ProjectClass> allProjectClasses) throws IOException {
        List<Commit> commitsInTicket = ticket.getCommitList();
        Release injectedVersion = ticket.getInjectedVersion();
        for(Commit commit: commitsInTicket){
            List<String> modifiedClassesNames = getModifiedClassesNames(commit.getRevCommit());
            Release release = commit.getRelease();
            for(String modifiedClass: modifiedClassesNames){
                labelBuggyClasses(modifiedClass, injectedVersion, release, allProjectClasses);
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
