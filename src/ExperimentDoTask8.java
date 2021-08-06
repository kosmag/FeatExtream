import java.io.IOException;


public class ExperimentDoTask8 {

    public ExperimentDoTask8() {
    }

    public static void main(String[] args) throws IOException {

        String cliCmd = "EvaluatePrequentialRegression -l (BufferLearner -l functions.AdaGrad -n 1 -m functions.AdaGrad -b relevance -p 7 -t 0) -s (ArffFileStream -f C:\\Users\\kosma\\Desktop\\MAGISTER\\FeatExtream\\data\\airlines.arff) -e (DelayedWindowRegressionPerformanceEvaluator -w 100000) -i 7200000";
        String[] myArgs8 = cliCmd.split(" ");

        moa.DoTask.main(myArgs8);
    }
}
