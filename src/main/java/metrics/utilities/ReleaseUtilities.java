package metrics.utilities;

import metrics.models.Release;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class ReleaseUtilities {
    private ReleaseUtilities(){

    }

    public static List<Release> returnValidAffectedVersions(JSONArray affectedVersionsArray, List<Release> releasesList) {
        List<Release> existingAffectedVersions = new ArrayList<>();
        for (int i = 0; i < affectedVersionsArray.length(); i++) {
            String affectedVersionName = affectedVersionsArray.getJSONObject(i).get("name").toString();
            for (Release release : releasesList) {
                if (Objects.equals(affectedVersionName, release.releaseName())) {
                    existingAffectedVersions.add(release);
                    break;
                }
            }
        }
        return existingAffectedVersions;
    }

    public static Release getReleaseAfterDate(Date specificDate, List<Release> releasesList) {
        for (Release release : releasesList) {
            if (release.releaseDate().after(specificDate)) {
                return release;
            }
        }
        return null;
    }
}
