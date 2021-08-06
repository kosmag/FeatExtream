import java.io.IOException;


public class ExperimentDoTask11 {

    public ExperimentDoTask11() {
    }

    public static void main(String[] args) throws IOException {

        String cliCmd = "EvaluatePrequentialRegression -l (BufferLearner -y 0,1,2,3 -l (rules.meta.RandomAMRules -l (AMRulesMultiTargetRegressor -L (rules.multilabel.functions.AdaptiveMultiTargetRegressor -l MultiTargetMeanRegressor -m MultiTargetPerceptronRegressor -e MeanAbsoluteDeviationMT) -A (OddsRatioScore -p CantellisInequality) -S MultiTargetVarianceRatio -e RelativeMeanAbsoluteDeviationMT -w InverseErrorWeightedVoteMultiLabel -O SelectAllOutputs -I SelectAllInputs -F NoFeatureRanking) -V UniformWeightedVoteMultiLabel -F NoFeatureRanking) -m functions.AdaGrad -p -1) -s (ArffFileStream -f C:\\Users\\kosma\\Desktop\\MAGISTER\\FeatExtream\\poc_dataset.arff) -i 100000 -f 1000 -q 1000 -d C:\\Users\\kosma\\Desktop\\MAGISTER\\FeatExtream\\runs\\poc_generator\\amrules_buf.csv";
        String[] myArgs8 = cliCmd.split(" ");

        moa.DoTask.main(myArgs8);
    }
}
