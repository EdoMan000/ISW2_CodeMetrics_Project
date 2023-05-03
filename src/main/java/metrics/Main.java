package metrics;

import metrics.controllers.ExtractMetrics;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;
import java.net.URISyntaxException;

public class Main {
    public static void main(String[] args) throws IOException, GitAPIException, URISyntaxException {
        ExtractMetrics.logStarting();
        ExtractMetrics.extractDataAndElaborate("BOOKKEEPER","https://github.com/EdoMan000/bookkeeper.git");
        ExtractMetrics.extractDataAndElaborate("OPENJPA", "https://github.com/EdoMan000/openjpa.git");
        ExtractMetrics.logTheEnd();
    }
}