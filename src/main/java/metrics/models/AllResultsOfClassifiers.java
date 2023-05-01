package metrics.models;

import java.util.ArrayList;
import java.util.List;

public class AllResultsOfClassifiers {

    private final List<ResultOfClassifier> allResultsList;
    private final List<ResultOfClassifier> avgResultsList;
    private final List<List<ResultOfClassifier>> allBasicLists;
    private final List<List<ResultOfClassifier>> allFeatureSelectionLists;
    private final List<List<ResultOfClassifier>> allSamplingLists;
    private final List<List<ResultOfClassifier>> allCompleteLists;

    public AllResultsOfClassifiers() {
        allResultsList = new ArrayList<>();
        avgResultsList = new ArrayList<>();
        allBasicLists = new ArrayList<>();
        allFeatureSelectionLists = new ArrayList<>();
        allSamplingLists = new ArrayList<>();
        allCompleteLists = new ArrayList<>();
        addInnerLists(allBasicLists);
        addInnerLists(allFeatureSelectionLists);
        addInnerLists(allSamplingLists);
        addInnerLists(allSamplingLists);
    }

    private void addInnerLists(List<List<ResultOfClassifier>> lists) {
        lists.add(new ArrayList<>());
        lists.add(new ArrayList<>());
        lists.add(new ArrayList<>());
    }

    public List<ResultOfClassifier> getAllResultsList() {
        return allResultsList;
    }

    public List<List<ResultOfClassifier>> getAllBasicLists() {
        return allBasicLists;
    }

    public List<List<ResultOfClassifier>> getAllFeatureSelectionLists() {
        return allFeatureSelectionLists;
    }

    public List<List<ResultOfClassifier>> getAllSamplingLists() {
        return allSamplingLists;
    }

    public List<List<ResultOfClassifier>> getAllCompleteLists() {
        return allCompleteLists;
    }

    public void createMergeOfResults() {
        addAllResultsOfList(allBasicLists);
        addAllResultsOfList(allFeatureSelectionLists);
        addAllResultsOfList(allSamplingLists);
        addAllResultsOfList(allCompleteLists);
    }

    private void addAllResultsOfList(List<List<ResultOfClassifier>> allBasicLists) {
        for (List<ResultOfClassifier> list : allBasicLists) {
            allResultsList.addAll(list);
        }
    }

    public List<ResultOfClassifier> getAvgResultsList() {
        return avgResultsList;
    }
}
