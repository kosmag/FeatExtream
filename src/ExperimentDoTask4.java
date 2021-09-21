import java.io.IOException;


public class ExperimentDoTask4 {

    public ExperimentDoTask4() {
    }

    public static void main(String[] args) throws IOException {

        String cliCmd =
                "EvaluatePrequential -l (meta.AdaptiveRandomForest -x (ADWINChangeDetector -a 0.001) -p (ADWINChangeDetector -a 0.01)) -s (ArffFileStream -f C:\\Users\\kosma\\Desktop\\MAGISTER\\FeatExtream\\result_sup.arff) -i 10000 -f 100 -q 100";

        String[] myArgs8 = cliCmd.split(" ");

        moa.DoTask.main(myArgs8);
    }
}