package metrics.models;

import java.util.Date;
import java.util.Objects;

public final class Release {
    private int id;
    private final String releaseName;
    private final Date releaseDate;

    public Release(String releaseName, Date releaseDate) {
        this.releaseName = releaseName;
        this.releaseDate = releaseDate;
    }

    public Release(int id, String releaseName, Date releaseDate) {
        this.id = id;
        this.releaseName = releaseName;
        this.releaseDate = releaseDate;
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

}
