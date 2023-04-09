package metrics;

import metrics.controllers.ExtractMetrics;

import java.io.IOException;
import java.text.ParseException;

public class Main {
    public static void main(String[] args) throws IOException, ParseException {
        ExtractMetrics.extractDataAndElaborate("BOOKKEEPER");
        //ExtractMetrics.extractDataAndElaborate("OPENJPA");
    }
}