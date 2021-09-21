import java.io.IOException;


public class ExperimentDoTask13 {

    public ExperimentDoTask13() {
    }

    public static void main(String[] args) throws IOException {

        String cliCmd = "EvaluatePrequentialRegression -l (BufferLearner -l (rules.functions.Perceptron) -n 28 -m (functions.AdaGrad) -c cluster -r 0.30000000000000004 -b relevance -p -1  -y -1 -q clustream -x 25) -s (ArffFileStream -f C:/Users/kosma/Desktop/MAGISTER/FeatExtream/data/airlines.arff) -i 50 -f 1 ";
        String[] myArgs8 = cliCmd.split(" ");

        moa.DoTask.main(myArgs8);
    }
}
