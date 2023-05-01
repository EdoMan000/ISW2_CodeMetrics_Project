package metrics.utilities;

import metrics.models.AllResultsOfClassifiers;
import metrics.models.ResultOfClassifier;

import java.util.List;

public class ResultOfClassifierUtils {
    private ResultOfClassifierUtils() {
    }

    public static void computeAvgOfAllLists(AllResultsOfClassifiers allResultsOfClassifiers){
        computeAvgOfLists(allResultsOfClassifiers.getAllBasicLists(), allResultsOfClassifiers);
        computeAvgOfLists(allResultsOfClassifiers.getAllFeatureSelectionLists(), allResultsOfClassifiers);
        computeAvgOfLists(allResultsOfClassifiers.getAllSamplingLists(), allResultsOfClassifiers);
        computeAvgOfLists(allResultsOfClassifiers.getAllCompleteLists(), allResultsOfClassifiers);
    }

    private static void computeAvgOfLists(List<List<ResultOfClassifier>> allLists, AllResultsOfClassifiers allResultsOfClassifiers) {
        for(List<ResultOfClassifier> list: allLists){
            ResultOfClassifier avgEvaluation = new ResultOfClassifier(list.get(0).getProjName(), 0,
                    list.get(0).getClassifierName(), list.get(0).hasFeatureSelection(), list.get(0).hasSampling());

            double precisionSum = 0;
            double recallSum = 0;
            double areaUnderROCSum = 0;
            double kappaSum = 0;

            double truePositivesSum = 0;
            double falsePositivesSum = 0;
            double trueNegativesSum = 0;
            double falseNegativesSum = 0;

            int numAucAveraged = 0;

            for(ResultOfClassifier evaluation : list) {
                double currentAuc = evaluation.getAreaUnderROC();

                precisionSum = precisionSum + evaluation.getPrecision();
                recallSum = recallSum + evaluation.getRecall();
                kappaSum = kappaSum + evaluation.getKappa();

                truePositivesSum = truePositivesSum + evaluation.getTruePositives();
                falsePositivesSum = falsePositivesSum + evaluation.getFalsePositives();
                trueNegativesSum = trueNegativesSum + evaluation.getTrueNegatives();
                falseNegativesSum = falseNegativesSum + evaluation.getFalseNegatives();

                //There are also AUC equal to NaN (this happens when there are no positive instances in testing set)
                if(!Double.isNaN(currentAuc)) {
                    areaUnderROCSum = areaUnderROCSum + evaluation.getAreaUnderROC();
                    numAucAveraged++;
                }

            }
            avgEvaluation.setPrecision(precisionSum/list.size());
            avgEvaluation.setRecall(recallSum/list.size());
            avgEvaluation.setKappa(kappaSum/list.size());

            avgEvaluation.setTruePositives(truePositivesSum/list.size());
            avgEvaluation.setFalsePositives(falsePositivesSum/list.size());
            avgEvaluation.setTrueNegatives(trueNegativesSum/list.size());
            avgEvaluation.setFalseNegatives(falseNegativesSum/list.size());

            if(numAucAveraged != 0) {
                avgEvaluation.setAreaUnderROC(areaUnderROCSum/numAucAveraged);
            }
            else {
                avgEvaluation.setAreaUnderROC(0);
            }
            allResultsOfClassifiers.getAvgResultsList().add(avgEvaluation);
        }
    }
}
