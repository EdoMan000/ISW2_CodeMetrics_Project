package metrics.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public final class Release {
    private int id;
    private final String releaseName;
    private final Date releaseDate;
    private List<Commit> commitList;

    public Release(String releaseName, Date releaseDate) {
        this.releaseName = releaseName;
        this.releaseDate = releaseDate;
        this.commitList = new ArrayList<>();
    }

    public Release(int id, String releaseName, Date releaseDate) {
        this.id = id;
        this.releaseName = releaseName;
        this.releaseDate = releaseDate;
        this.commitList = new ArrayList<>();
    }

    public int id() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String releaseName() {
        return releaseName;
    }

    public Date releaseDate() {
        return releaseDate;
    }

    public void addCommit(Commit newCommit) {
        if(!commitList.contains(newCommit)){
            this.commitList.add(newCommit);
        }
    }

    public List<Commit> getCommitList(){
        return commitList;
    }
}
