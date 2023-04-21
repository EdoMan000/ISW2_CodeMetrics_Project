package metrics.models;

import java.util.ArrayList;
import java.util.List;

public class ProjectClass {
    private final String name;
    private final String contentOfClass;
    private final Release release;
    private boolean bugged;
    private List<Commit> commitsThatModify;

    public ProjectClass(String name, String contentOfClass, Release release) {

        this.name = name;
        this.contentOfClass = contentOfClass;
        this.release = release;
        this.bugged = false;
        this.commitsThatModify = new ArrayList<>();
    }

    public List<Commit> getCommitsThatModify() {
        return commitsThatModify;
    }

    public void setCommitsThatModify(List<Commit> commitsThatModify) {
        this.commitsThatModify = commitsThatModify;
    }

    public boolean isBugged() {
        return bugged;
    }

    public void setBugged(boolean bugged) {
        this.bugged = bugged;
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
}
