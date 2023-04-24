package metrics.models;

import java.util.ArrayList;
import java.util.List;

public class ProjectClass {
    private final String name;
    private final String contentOfClass;
    private final Release release;

    private final Metrics metrics;
    private List<Commit> commitsThatModify;
    private final List<Integer> LOCAddedByClass;
    private final List<Integer> LOCRemovedByClass;

    public ProjectClass(String name, String contentOfClass, Release release) {

        this.name = name;
        this.contentOfClass = removeComments(contentOfClass);
        this.release = release;
        metrics = new Metrics();
        commitsThatModify = new ArrayList<>();
        LOCAddedByClass = new ArrayList<>();
        LOCRemovedByClass = new ArrayList<>();
    }

    private String removeComments(String contentOfClass) {
        List<String> lines = new java.util.ArrayList<>(List.of(contentOfClass.split("\r\n|\r|\n")));
        lines.removeIf(line -> line.replace("\t","").replace(" ","").startsWith("//"));
        lines.removeIf(line -> line.replace("\t","").replace(" ","").startsWith("/*"));
        lines.removeIf(line -> line.replace("\t","").replace(" ","").startsWith("*"));
        lines.removeIf(line -> line.replace(" ","").endsWith("*/"));
        StringBuilder stringBuilder = new StringBuilder();
        for(String line: lines){
            stringBuilder.append(line).append("\r\n");
        }
        return stringBuilder.toString();
    }

    public List<Commit> getCommitsThatModify() {
        return commitsThatModify;
    }

    public void setCommitsThatModify(List<Commit> commitsThatModify) {
        this.commitsThatModify = commitsThatModify;
    }

    public Release getRelease() {
        return release;
    }

    public String getContentOfClass() {
        return contentOfClass;
    }

    public String getName() {
        return name;
    }


    public Metrics getMetrics() {
        return metrics;
    }

    public List<Integer> getLOCAddedByClass() {
        return LOCAddedByClass;
    }

    public void addLOCAddedByClass(Integer LOCAddedByEntry) {
        LOCAddedByClass.add(LOCAddedByEntry);
    }

    public List<Integer> getLOCRemovedByClass() {
        return LOCRemovedByClass;
    }

    public void addLOCRemovedByClass(Integer LOCRemovedByEntry) {
        LOCRemovedByClass.add(LOCRemovedByEntry);
    }
}
