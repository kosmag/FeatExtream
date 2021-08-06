import java.io.IOException;


public class ExperimentDoTask6 {

    public ExperimentDoTask6() {
    }

    public static void main(String[] args) throws IOException {

        String cliCmd = "EvaluatePrequentialRegression -l (BufferLearner -l (meta.AdaptiveRandomForestRegressor -l (ARFFIMTDD -k 14 -s VarianceReductionSplitCriterion -g 50 -c 0.01) -x (ADWINChangeDetector -a 0.001) -p (ADWINChangeDetector -a 0.01)) -n 50 -m (meta.AdaptiveRandomForestRegressor -x (ADWINChangeDetector -a 0.001) -p (ADWINChangeDetector -a 0.01)) -c cluster -p 15 -t 1,6,8,10 -i 16 -x 8) -s (ArffFileStream -f C:\\Users\\kosma\\Desktop\\MAGISTER\\FeatExtream\\result_sup_slim.arff) -i 1000000 -f 1000 -q 1000 -d tmpout.csv";
        String[] myArgs8 = cliCmd.split(" ");

        moa.DoTask.main(myArgs8);
    }
}