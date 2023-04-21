package metrics;

import metrics.controllers.ExtractMetrics;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;
import java.text.ParseException;

public class Main {
    public static void main(String[] args) throws IOException, ParseException, GitAPIException {
        ExtractMetrics.extractDataAndElaborate("BOOKKEEPER","https://github.com/EdoMan000/bookkeeper.git");
        //ExtractMetrics.extractDataAndElaborate("OPENJPA", "https://github.com/EdoMan000/openjpa.git");
    }
}