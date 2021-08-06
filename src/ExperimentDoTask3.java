import java.io.IOException;


public class ExperimentDoTask3 {

    public ExperimentDoTask3() {
    }

    public static void main(String[] args) throws IOException {
//        String[] myArgs8 = {"EvaluatePrequential", "-l", "moa.classifiers.functions.BufferClassifier", "-s",
//                "moa.streams.generators.RandomRBFGenerator", "-i", "1000"};
//
//        moa.DoTask.main(myArgs8);

        String cliCmd ="EvaluatePrequential -l (BufferLearner -l (meta.AdaptiveRandomForest -l (ARFHoeffdingTree -k 17 -e 2000000 -g 50 -c 0.01) -o (Specified m (integer value)) -m 17 -x (ADWINChangeDetector -a 0.001) -p (ADWINChangeDetector -a 0.01)) -n 100 -r 0.7002999999999999 -m (meta.AdaptiveRandomForestRegressor -l (ARFFIMTDD -k 5 -s VarianceReductionSplitCriterion -g 50 -c 0.01) -x (ADWINChangeDetector -a 0.001) -p (ADWINChangeDetector -a 0.01)) -c cep -b relevance -p -1 -t 0,1,2) -s (ArffFileStream -f D:\\Downloads\\electricity_arff.arff) -i 10000 -f 100 -q 100";

        String[] myArgs8 = cliCmd.split(" ");

        moa.DoTask.main(myArgs8);
    }
}