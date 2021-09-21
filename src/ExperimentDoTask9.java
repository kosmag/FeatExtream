import java.io.IOException;


public class ExperimentDoTask9 {

    public ExperimentDoTask9() {
    }

    public static void main(String[] args) throws IOException {

        String cliCmd = "EvaluatePrequentialRegression -l rules.functions.TargetMean -s (ArffFileStream -f C:\\Users\\kosma\\Desktop\\MAGISTER\\FeatExtream\\result_90500.arff) -e (DelayedWindowRegressionPerformanceEvaluator -i 42) -f 10000 -q 10000";
        String[] myArgs8 = cliCmd.split(" ");

        moa.DoTask.main(myArgs8);
    }
}
