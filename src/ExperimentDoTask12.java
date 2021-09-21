import java.io.IOException;


public class ExperimentDoTask12 {

    public ExperimentDoTask12() {
    }

    public static void main(String[] args) throws IOException {

        String cliCmd = "EvaluatePrequentialRegression -l (BufferLearner -l (meta.AdaptiveRandomForestRegressor -l (ARFFIMTDD -k 14 -s VarianceReductionSplitCriterion -g 50 -c 0.01) -m 80 -x (ADWINChangeDetector -a 0.001) -p (ADWINChangeDetector -a 0.01)) -n 1 -m (meta.AdaptiveRandomForestRegressor -x (ADWINChangeDetector -a 0.001) -p (ADWINChangeDetector -a 0.01)) -c cluster -r 1 -b random -p 14 -t 0,5,7,9 -y -1 -q clustree -x 8) -s (ArffFileStream -f C:/Users/kosma/Desktop/MAGISTER/FeatExtream/result_sup_slim.arff) -i 32000 -f 1000 -q 1000 -d runs/tuning/20210829/cluster/n/vavel_slim_cluster_1_8_14_0,5,7,9_-1_clustree_random_100.csv";
        String[] myArgs8 = cliCmd.split(" ");

        moa.DoTask.main(myArgs8);
    }
}
