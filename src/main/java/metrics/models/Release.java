package metrics.models;

import java.util.Date;

public record Release(int id, String releaseName, Date releaseDate) {
}
