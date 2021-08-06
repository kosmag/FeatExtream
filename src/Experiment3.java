import com.yahoo.labs.samoa.instances.Instance;
import moa.classifiers.BufferLearner;
import moa.core.TimingUtils;
import moa.streams.ArffFileStream;

import java.io.IOException;
import java.util.Arrays;


public class Experiment3 {

    public Experiment3() {
    }

    public void run(int numInstances, boolean isTesting) {
        BufferLearner learner = new BufferLearner(
                "moa.classifiers.meta.AdaptiveRandomForestRegressor",
                10,
                1.0,
                "moa.classifiers.meta.AdaptiveRandomForestRegressor",
                1,
                "naive",
                "random",
                1,
                "3,11",
                "-1",
                10,
                "clustree"
                );
        ArffFileStream stream = new ArffFileStream("C:\\Users\\kosma\\Desktop\\MAGISTER\\Vavel arff\\2018-05-23\\part-0-1.arff", 10);
        stream.prepareForUse();

        learner.prepareForUse();
        learner.setModelContext(stream.getHeader());

        int numberSamples = 0;
        double absoluteError = 0;
        boolean preciseCPUTiming = TimingUtils.enablePreciseTiming();
        long evaluateStartTime = TimingUtils.getNanoCPUTimeOfCurrentThread();
        while (stream.hasMoreInstances() && numberSamples < numInstances) {

            Instance trainInst = stream.nextInstance().getData();
            if (true || !nearlyEquals(trainInst.classValue(), 0.0)) {
                double outputVote = Arrays.stream(learner.getPredictionForInstance(trainInst).getVotes()).average().getAsDouble();
                absoluteError += Math.abs(outputVote - trainInst.classValue());
                System.out.println("Output: " + outputVote + "; Actual: " + trainInst.classValue());
                numberSamples++;
                learner.trainOnInstance(trainInst);
                if (numberSamples > 0 && numberSamples % 100 == 0) {
                    double mae = absoluteError / (double) 100;
                    double time = TimingUtils.nanoTimeToSeconds(TimingUtils.getNanoCPUTimeOfCurrentThread() - evaluateStartTime);
                    System.out.println("Last 100 instances processed with " + mae + "MAE at " + time + " seconds.");
                    absoluteError = 0;
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        Experiment3 exp = new Experiment3();
        exp.run(8000, true);
    }

    public boolean nearlyEquals(double d1, double d2) {
        double eps = 0.000001d;
        return Math.abs(d1 - d2) < eps;
    }
}