package metrics.utilities;

import metrics.models.Release;
import org.json.JSONArray;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
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
        existingAffectedVersions.sort(Comparator.comparing(Release::releaseDate));
        return existingAffectedVersions;
    }

    public static Release getReleaseAfterOrEqualDate(LocalDate specificDate, List<Release> releasesList) {
        releasesList.sort(Comparator.comparing(Release::releaseDate));
        for (Release release : releasesList) {
            if (!release.releaseDate().isBefore(specificDate)) {
                return release;
            }
        }
        return null;
    }

    public static void printRelease(Release release) {
        System.out.println("Release[id= " + release.id()
                + ", releaseName= " + release.releaseName()
                + ", releaseDate= " + release.releaseDate()
                + ", numOfCommits= " + release.getCommitList().size()
                + "]\n"
        );
    }
}
