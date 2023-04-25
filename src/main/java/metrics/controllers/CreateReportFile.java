package metrics.controllers;

import metrics.models.Commit;
import metrics.models.Release;
import metrics.models.Ticket;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CreateReportFile {

    private enum ReportTypes {
        RELEASES("/Releases"),
        TICKETS("/Tickets"),
        COMMITS("/Commits"),
        SUMMARY("/Summary");

        private final String fileName;

        ReportTypes(String fileName) {
            this.fileName = fileName;
        }

        public String toString() {
            return fileName;
        }
    }

    private CreateReportFile() {
    }

    public static void writeOnReportFiles(String projName, List<Release> releaseList, List<Ticket> ticketList, List<Commit> commitList, List<Commit> filteredCommitsOfIssues, List<Integer> buggyClassesPerRelease) {
        FileWriter fileWriter;
        try {
            File file = new File("outputFiles/reportFiles/" + projName);
            if (!file.exists()) {
                boolean created = file.mkdirs();
                if (!created) {
                    throw new Exception();
                }
            }
            for(ReportTypes reportType: ReportTypes.values()){
                file = new File("outputFiles/reportFiles/" + projName + reportType.toString() + ".txt");
                fileWriter = new FileWriter(file);
                fileWriter.append("---------- ")
                        .append(String.valueOf(reportType))
                        .append(" List/ ---------\n\n");
                switch (reportType) {
                    case RELEASES -> appendReleasesInfo(releaseList, fileWriter);
                    case TICKETS -> appendTicketsInfo(ticketList, fileWriter);
                    case COMMITS -> appendCommitsInfo(commitList, fileWriter);
                    case SUMMARY -> appendSummaryInfo(releaseList, ticketList, commitList, filteredCommitsOfIssues, buggyClassesPerRelease, fileWriter);
                }
                try {
                    fileWriter.flush();
                    fileWriter.close();
                } catch (IOException e) {
                    System.out.println("Error while flushing/closing fileWriter !!!");
                }
            }
        } catch (Exception e) {
            System.out.println("Error in reportFiles writer");
        }
    }

    private static void appendSummaryInfo(List<Release> releaseList, List<Ticket> ticketList, List<Commit> commitList, List<Commit> filteredCommitsOfIssues, List<Integer> buggyClassesPerRelease, FileWriter fileWriter) throws IOException {
        fileWriter.append("----------------------------------------------------------\n")
                .append("EXTRACTION INFO:\n")
                .append(String.valueOf(releaseList.size())).append(" RELEASES \n")
                .append(String.valueOf(ticketList.size())).append(" TICKETS \n")
                .append(String.valueOf(commitList.size())).append(" TOTAL COMMITS \n")
                .append(String.valueOf(filteredCommitsOfIssues.size())).append(" COMMITS CONTAINING BUG-ISSUES")
                .append("\n----------------------------------------------------------\n\n");
        for (Release release : releaseList) {
            fileWriter.append("RELEASE ").append(release.releaseName()).append(" HAS ").append(String.valueOf(buggyClassesPerRelease.get(release.id() - 1))).append(" BUGGY CLASSES\n\n");
        }
    }

    private static void appendCommitsInfo(List<Commit> commitList, FileWriter fileWriter) throws IOException {
        for (Commit commit: commitList){
            RevCommit revCommit = commit.getRevCommit();
            Ticket ticket = commit.getTicket();
            Release release = commit.getRelease();
            fileWriter.append("Commit[ID= ").append(revCommit.getName())
                    //.append(", committer= ").append(revCommit.getCommitterIdent().getName())
                    //.append(", message= ").append(revCommit.getFullMessage())
                    .append((ticket == null) ? "" : ", ticket= " + commit.getTicket().getTicketKey())
                    .append(", release= ").append(release.releaseName())
                    .append(", creationDate= ").append(String.valueOf(LocalDate.parse((new SimpleDateFormat("yyyy-MM-dd").format(revCommit.getCommitterIdent().getWhen()))))).append("]\n\n");
        }
    }

    private static void appendTicketsInfo(List<Ticket> ticketList, FileWriter fileWriter) throws IOException {
        List<Ticket> ticketOrderedByCreation = new ArrayList<>(ticketList);
        ticketOrderedByCreation.sort(Comparator.comparing(Ticket::getCreationDate));
        for (Ticket ticket : ticketOrderedByCreation) {
            List<String> iDs = new ArrayList<>();
            for(Release release : ticket.getAffectedVersions()) {
                iDs.add(release.releaseName());
            }
            fileWriter.append("Ticket[key= ").append(ticket.getTicketKey())
                    .append(", injectedVersion= ") .append(ticket.getInjectedVersion().releaseName())
                    .append(", openingVersion= ").append(ticket.getOpeningVersion().releaseName())
                    .append(", fixedVersion= ") .append(ticket.getFixedVersion().releaseName())
                    .append(", affectedVersions= ").append(String.valueOf(iDs))
                    //.append(", numOfCommits= ").append(String.valueOf(ticket.getCommitList().size()))
                    .append(", creationDate= ").append(String.valueOf(ticket.getCreationDate()))
                    .append(", resolutionDate= ").append(String.valueOf(ticket.getResolutionDate())).append("]\n\n");
        }
    }

    private static void appendReleasesInfo(List<Release> releaseList, FileWriter fileWriter) throws IOException {
        for (Release release : releaseList) {
            fileWriter.append("Release[id= ").append(String.valueOf(release.id()))
                    .append(", releaseName= ").append(release.releaseName())
                    .append(", releaseDate= ").append(String.valueOf(release.releaseDate()))
                    .append(", numOfCommits= ").append(String.valueOf(release.getCommitList().size())).append("]\n\n");
        }
    }
}
