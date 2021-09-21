import java.io.IOException;


public class ExperimentDoTask5 {

    public ExperimentDoTask5() {
    }

    public static void main(String[] args) throws IOException {

        String cliCmd =
                "EvaluatePrequentialRegression -l (BufferLearner -l (meta.AdaptiveRandomForestRegressor -l (ARFFIMTDD -k 28 -s VarianceReductionSplitCriterion -g 50 -c 0.01) -x (ADWINChangeDetector -a 0.001) -p (ADWINChangeDetector -a 0.01)) -n 100 -m (meta.AdaptiveRandomForestRegressor -x (ADWINChangeDetector -a 0.001) -p (ADWINChangeDetector -a 0.01)) -r 0.2548459 -b relevance -c cluster -p 38 -t 2,8,17,18,23 -i 42 -x 10 -q clustream -y 2,3,4,6,29) -s (ArffFileStream -f C:\\Users\\kosma\\Desktop\\MAGISTER\\FeatExtream\\result_sup.arff) -f 1000 -q 1000";
        String[] myArgs8 = cliCmd.split(" ");

        moa.DoTask.main(myArgs8);
    }
}