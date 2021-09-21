import java.io.IOException;


public class ExperimentDoTask10 {

    public ExperimentDoTask10() {
    }

    public static void main(String[] args) throws IOException {

        String cliCmd = "EvaluatePrequentialRegression -l (BufferLearner -l (meta.AdaptiveRandomForestRegressor -l (ARFFIMTDD -k 14 -s VarianceReductionSplitCriterion -g 50 -c 0.01) -x (ADWINChangeDetector -a 0.001) -p (ADWINChangeDetector -a 0.01)) -n 100 -m (meta.AdaptiveRandomForestRegressor -x (ADWINChangeDetector -a 0.001) -p (ADWINChangeDetector -a 0.01)) -c cluster -p 14 -t 0,5,7,9 -x 8) -s (ArffFileStream -f C:\\Users\\kosma\\Desktop\\MAGISTER\\FeatExtream\\result_sup_slim.arff) -i 32000 -f 1000 -q 1000";
        String[] myArgs8 = cliCmd.split(" ");

        moa.DoTask.main(myArgs8);
    }
}
