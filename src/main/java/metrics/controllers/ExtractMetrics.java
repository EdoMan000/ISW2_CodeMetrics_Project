package metrics.controllers;

import metrics.models.*;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static metrics.controllers.CreateArffOrCsvDataFiles.writeOnArffOrCsvDataFile;
import static metrics.controllers.CreateCsvFinalResultsFile.writeCsvFinalResultsFile;
import static metrics.controllers.CreateReportFiles.writeOnReportFiles;


public class ExtractMetrics {
    private static final Logger logger = Logger.getLogger(ExtractMetrics.class.getName());
    private ExtractMetrics(){}

    public static void extractDataAndElaborate(String projName, String repoURL) throws IOException, URISyntaxException, GitAPIException {
        String loggerString = projName + " DATA EXTRACTION STARTED...\n\n";
        logger.info(loggerString);
        ExtractInfoFromJira jiraExtractor = new ExtractInfoFromJira(projName.toUpperCase());
        List<Release> releaseList = jiraExtractor.extractAllReleases();
        loggerString = projName + " RELEASES EXTRACTED - [*OK*]\n\n";
        logger.info(loggerString);
        ExtractInfoFromGit gitExtractor = new ExtractInfoFromGit(projName, repoURL, releaseList);
        List<Commit> commitList = gitExtractor.extractAllCommits();
        loggerString = projName + " COMMITS EXTRACTED - [*OK*]\n\n";
        logger.info(loggerString);
        releaseList = gitExtractor.getReleaseList();
        List<Ticket> ticketList = jiraExtractor.extractAllTickets(releaseList);
        loggerString = projName + " TICKETS EXTRACTED - [*OK*]\n\n";
        gitExtractor.setTicketList(ticketList);
        logger.info(loggerString);
        List<Commit> filteredCommitsOfIssues = gitExtractor.filterCommitsOfIssues(commitList);
        loggerString = projName + " FOUND COMMITS CONTAINING ISSUES IN COMMENT - [*OK*]\n\n";
        logger.info(loggerString);
        ticketList = gitExtractor.getTicketList();
        List<ProjectClass> allProjectClasses = gitExtractor.extractAllProjectClasses(commitList, releaseList.size());
        loggerString = projName + " CLASSES EXTRACTED - [*OK*]\n\n";
        logger.info(loggerString);
        ComputeMetrics metricsExtractor = new ComputeMetrics(gitExtractor, allProjectClasses, filteredCommitsOfIssues);
        metricsExtractor.computeAllMetrics();
        loggerString = projName + " METRICS COMPUTED - [*OK*]\n\n";
        logger.info(loggerString);
        writeOnReportFiles(projName, releaseList, ticketList, commitList, filteredCommitsOfIssues);
        //WALK FORWARD APPROACH
        loggerString = projName + " STARTING WALK FORWARD TO BUILD TRAINING AND TESTING SETS - [*OK*]\n\n";
        logger.info(loggerString);
        int idOfLastConsideredRelease = releaseList.get((releaseList.size()/2)-1).id();
        for(int i = 1; i <= idOfLastConsideredRelease; i++){
            List<Release> firstIReleases = new ArrayList<>(releaseList);
            int finalI = i;
            firstIReleases.removeIf(release -> release.id() > finalI);
            List<Ticket> firstITickets = new ArrayList<>(ticketList);
            firstITickets.removeIf(ticket -> ticket.getFixedVersion().id() > firstIReleases.get(firstIReleases.size()-1).id());
            List<ProjectClass> firstIProjectClassesTraining = new ArrayList<>(allProjectClasses);
            firstIProjectClassesTraining.removeIf(projectClass -> projectClass.getRelease().id() > firstIReleases.get(firstIReleases.size()-1).id());
            gitExtractor.completeClassesInfo(firstITickets, firstIProjectClassesTraining);
            writeOnArffOrCsvDataFile(projName, firstIReleases, firstIProjectClassesTraining, "Training", true, finalI);
            writeOnArffOrCsvDataFile(projName, firstIReleases, firstIProjectClassesTraining, "Training", false, finalI);
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
            //no need to re-compute buggyness because is already computed with all the tickets before doing walk forward
            writeOnArffOrCsvDataFile(projName, testingSetReleaseList, firstIProjectClassesTesting, "Testing", true, finalI);
            writeOnArffOrCsvDataFile(projName, testingSetReleaseList, firstIProjectClassesTesting, "Testing", false, finalI);
            if(i==1){
                loggerString = projName + " TESTING SET BUILT ON FIRST RELEASE - [*OK*]\n\n";
            }else{
                loggerString = projName + " TESTING SET BUILT ON RELEASES 1->" + i + " - [*OK*]\n\n";
            }
            logger.info(loggerString);
        }
        ExtractInfoFromWeka wekaExtractor = new ExtractInfoFromWeka(projName, (releaseList.size()/2)-1);
        AllResultsOfClassifiers resultOfClassifierList = wekaExtractor.retrieveAllEvaluationsFromClassifiers();
        writeCsvFinalResultsFile(projName, resultOfClassifierList.getAllResultsList(), "completeInfo");
        writeCsvFinalResultsFile(projName, resultOfClassifierList.getAvgResultsList(), "avg");
        //deleteDirectory(projName.toLowerCase() + "Temp")
        //deleteDirectory("outputFiles")
    }
}
