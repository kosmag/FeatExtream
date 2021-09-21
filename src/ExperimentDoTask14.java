import java.io.IOException;


public class ExperimentDoTask14 {

    public ExperimentDoTask14() {
    }

    public static void main(String[] args) throws IOException {

        String cliCmd = "EvaluatePrequential -l (meta.AdaptiveRandomForest -l (ARFHoeffdingTree -k 30 -e 2000000 -g 50 -c 0.01) -s 10 -x (ADWINChangeDetector -a 0.001) -p (ADWINChangeDetector -a 0.01)) -s (ArffFileStream -f C:\\Users\\kosma\\Desktop\\MAGISTER\\FeatExtream\\result_90500_clf.arff) -e (DelayedWindowClassificationPerformanceEvaluator -i 41) -i 90000 -f 1000";
        String[] myArgs8 = cliCmd.split(" ");

        moa.DoTask.main(myArgs8);
    }
}
