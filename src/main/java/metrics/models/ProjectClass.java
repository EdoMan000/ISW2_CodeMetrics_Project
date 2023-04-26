package metrics.models;

import java.util.ArrayList;
import java.util.List;

public class ProjectClass {
    private final String name;
    private final String contentOfClass;
    private final Release release;
    private final Metrics metrics;
    private final List<Commit> commitsThatTouchTheClass;
    private final List<Integer> lOCAddedByClass;
    private final List<Integer> lOCRemovedByClass;

    public ProjectClass(String name, String contentOfClass, Release release) {
        this.name = name;
        this.contentOfClass = contentOfClass;
        this.release = release;
        metrics = new Metrics();
        commitsThatTouchTheClass = new ArrayList<>();
        lOCAddedByClass = new ArrayList<>();
        lOCRemovedByClass = new ArrayList<>();
    }

    public List<Commit> getCommitsThatTouchTheClass() {
        return commitsThatTouchTheClass;
    }
    public void addCommitThatTouchesTheClass(Commit commit) {
        this.commitsThatTouchTheClass.add(commit);
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
        return lOCAddedByClass;
    }

    public void addLOCAddedByClass(Integer lOCAddedByEntry) {
        lOCAddedByClass.add(lOCAddedByEntry);
    }

    public List<Integer> getLOCRemovedByClass() {
        return lOCRemovedByClass;
    }

    public void addLOCRemovedByClass(Integer lOCRemovedByEntry) {
        lOCRemovedByClass.add(lOCRemovedByEntry);
    }


}
