import moa.core.TimingUtils;
import moa.streams.ArffFileStream;
import moa.classifiers.meta.AdaptiveRandomForestRegressor;
import com.yahoo.labs.samoa.instances.Instance;

import java.io.IOException;
import java.util.Arrays;


public class Experiment {

    public Experiment() {
    }

    public void run(int numInstances, boolean isTesting) {
        AdaptiveRandomForestRegressor learner = new AdaptiveRandomForestRegressor();

        ArffFileStream stream = new ArffFileStream("C:\\Users\\kosma\\Desktop\\MAGISTER\\Vavel arff\\2018-05-23\\part-0-1.arff", 10);
        stream.prepareForUse();

        learner.setModelContext(stream.getHeader());
        learner.prepareForUse();

        int numberSamples = 0;
        double absoluteError = 0;
        boolean preciseCPUTiming = TimingUtils.enablePreciseTiming();
        long evaluateStartTime = TimingUtils.getNanoCPUTimeOfCurrentThread();
        while (stream.hasMoreInstances() && numberSamples < numInstances) {

            Instance trainInst = stream.nextInstance().getData();
            if (true || !nearlyEquals(trainInst.classValue(), 0.0)) {
                double outputVote = Arrays.stream(learner.getPredictionForInstance(trainInst).getVotes()).average().getAsDouble();
                absoluteError += Math.abs(outputVote - trainInst.classValue());
//                System.out.println("Output: " + outputVote + "; Actual: " + trainInst.classValue());
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
        Experiment exp = new Experiment();
        exp.run(8000, true);
    }

    public boolean nearlyEquals(double d1, double d2) {
        double eps = 0.000001d;
        return Math.abs(d1 - d2) < eps;
    }
}