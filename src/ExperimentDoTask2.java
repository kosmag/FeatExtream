import java.io.IOException;


public class ExperimentDoTask2 {

    public ExperimentDoTask2() {
    }

    public static void main(String[] args) throws IOException {
//        String[] myArgs8 = {"EvaluatePrequential", "-l", "moa.classifiers.functions.BufferClassifier", "-s",
//                "moa.streams.generators.RandomRBFGenerator", "-i", "1000"};
//
//        moa.DoTask.main(myArgs8);

        String cliCmd = "EvaluatePrequentialRegression -l functions.AdaGrad -s (ArffFileStream -f C:\\Users\\kosma\\Desktop\\MAGISTER\\FeatExtream\\data\\airlines.arff) -e (WindowRegressionPerformanceEvaluator -w 100000) -i 7200000";
        String[] myArgs8 = cliCmd.split(" ");

        moa.DoTask.main(myArgs8);
    }
}