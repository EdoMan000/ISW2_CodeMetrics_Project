package metrics.controllers;

import metrics.models.Commit;
import metrics.models.ProjectClass;
import metrics.models.Release;
import metrics.models.Ticket;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static metrics.controllers.CreateArffOrCsvFiles.writeOnArffFile;
import static metrics.controllers.CreateReportFiles.writeOnReportFiles;


public class ExtractMetrics {
    private static final Logger logger = Logger.getLogger(ExtractMetrics.class.getName());
    private ExtractMetrics(){}

    public static void extractDataAndElaborate(String projName, String repoURL) throws IOException, GitAPIException, URISyntaxException {
        String loggerString = projName + " DATA EXTRACTION STARTED...\n\n";
        logger.info(loggerString);
        ExtractInfoFromJira jiraExtractor = new ExtractInfoFromJira(projName.toUpperCase());
        List<Release> releaseList = jiraExtractor.extractAllReleases();
        loggerString = projName + " RELEASES EXTRACTED - [*OK*]\n\n";
        logger.info(loggerString);
        List<Ticket> ticketList = jiraExtractor.extractAllTickets(releaseList);
        loggerString = projName + " TICKETS EXTRACTED - [*OK*]\n\n";
        logger.info(loggerString);
        ExtractInfoFromGit gitExtractor = new ExtractInfoFromGit(projName, repoURL, releaseList, ticketList);
        List<Commit> commitList = gitExtractor.extractAllCommits();
        List<Commit> filteredCommitsOfIssues = gitExtractor.filterCommitsOfIssues(commitList);
        loggerString = projName + " COMMITS EXTRACTED - [*OK*]\n\n";
        logger.info(loggerString);
        List<ProjectClass> allProjectClasses = gitExtractor.extractAllProjectClasses(commitList, releaseList.size());
        loggerString = projName + " CLASSES EXTRACTED - [*OK*]\n\n";
        logger.info(loggerString);
        ComputeMetrics metricsExtractor = new ComputeMetrics(gitExtractor, allProjectClasses, filteredCommitsOfIssues);
        metricsExtractor.computeAllMetrics();
        loggerString = projName + " METRICS COMPUTED - [*OK*]\n\n";
        logger.info(loggerString);
        writeOnReportFiles(projName, releaseList, gitExtractor.getTicketList(), commitList, filteredCommitsOfIssues);
        //WALK FORWARD APPROACH
        loggerString = projName + " STARTING WALK FORWARD TO BUILD TRAINING AND TESTING SETS - [*OK*]\n\n";
        logger.info(loggerString);
        int idOfLastRelease = releaseList.get(releaseList.size()/2).id();
        for(int i = 1; i <= idOfLastRelease; i++){
            List<Release> firstIReleases = new ArrayList<>(releaseList);
            int finalI = i;
            firstIReleases.removeIf(release -> release.id() > finalI);
            List<Ticket> firstITickets = new ArrayList<>(ticketList);
            firstITickets.removeIf(ticket -> ticket.getFixedVersion().id() > firstIReleases.get(firstIReleases.size()-1).id());
            List<ProjectClass> firstIProjectClassesTraining = new ArrayList<>(allProjectClasses);
            firstIProjectClassesTraining.removeIf(projectClass -> projectClass.getRelease().id() > firstIReleases.get(firstIReleases.size()-1).id());
            gitExtractor.completeClassesInfo(firstITickets, firstIProjectClassesTraining);
            writeOnArffFile(projName, firstIReleases, firstIProjectClassesTraining, "Training");
            if(i==1){
                loggerString = projName + " TRAINING SET BUILT ON FIRST RELEASE - [*OK*]\n\n";
            }else{
                loggerString = projName + " TRAINING SET BUILT ON RELEASES 1->" + i + " - [*OK*]\n\n";
            }
            logger.info(loggerString);
            List<Release> testingSetReleaseList = new ArrayList<>();
            for(Release release: releaseList){
                if(release.id() == firstIReleases.get(firstIReleases.size()-1).id() + 1){
                    testingSetReleaseList.add(release);
                    break;
                }
            }
            List<ProjectClass> firstIProjectClassesTesting = new ArrayList<>(allProjectClasses);
            firstIProjectClassesTesting.removeIf(projectClass -> projectClass.getRelease().id() != testingSetReleaseList.get(0).id());
            writeOnArffFile(projName, testingSetReleaseList, firstIProjectClassesTesting, "Testing");
            if(i==1){
                loggerString = projName + " TESTING SET BUILT ON FIRST RELEASE - [*OK*]\n\n";
            }else{
                loggerString = projName + " TESTING SET BUILT ON RELEASES 1->" + i + " - [*OK*]\n\n";
            }
            logger.info(loggerString);
        }
        //ExtractInfoFromGit.deleteDirectory(projName.toLowerCase() + "Temp")
        //ExtractInfoFromGit.deleteDirectory("outputFiles")
    }
}
