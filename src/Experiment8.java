import com.yahoo.labs.samoa.instances.Instance;
import moa.classifiers.BufferLearner;
import moa.core.TimingUtils;
import moa.core.Utils;
import moa.streams.generators.RandomRBFGenerator;

import java.io.IOException;


public class Experiment8 {

    public Experiment8() {
    }

    public void run(int numInstances, boolean isTesting) {
        BufferLearner learner = new BufferLearner(
                "moa.classifiers.meta.AdaptiveRandomForest",
                10,
                0.1,
                "moa.classifiers.meta.AdaptiveRandomForestRegressor",
                1,
                "cep",
                "relevance",
                1,
                "3,11",
                "-1",
                10,
                "clustree"
        );
        RandomRBFGenerator stream = new RandomRBFGenerator();
        stream.prepareForUse();

        learner.prepareForUse();
        learner.setModelContext(stream.getHeader());

        int numberSamples = 0;
        int numberSamplesCorrect = 0;
        boolean preciseCPUTiming = TimingUtils.enablePreciseTiming();
        long evaluateStartTime = TimingUtils.getNanoCPUTimeOfCurrentThread();
        while (stream.hasMoreInstances() && numberSamples < numInstances) {

            Instance trainInst = stream.nextInstance().getData();

            double outputVote = Utils.maxIndex(learner.getVotesForInstance(trainInst));
            System.out.println("Output: " + outputVote + "; Actual: " + trainInst.classValue());
            if (learner.correctlyClassifies(trainInst)){
                numberSamplesCorrect++;
            }
            numberSamples++;
            learner.trainOnInstance(trainInst);
            if (numberSamples > 0 && numberSamples % 100 == 0) {
                double accuracy = 100.0 * (double) numberSamplesCorrect/ (double) 100;
                double time = TimingUtils.nanoTimeToSeconds(TimingUtils.getNanoCPUTimeOfCurrentThread() - evaluateStartTime);
                System.out.println("Last 100 instances processed with " + accuracy + "% accuracy at " + time + " seconds.");
                numberSamplesCorrect = 0;
            }

        }
    }

    public static void main(String[] args) throws IOException {
        Experiment8 exp = new Experiment8();
        exp.run(8000, true);
    }

    public boolean nearlyEquals(double d1, double d2) {
        double eps = 0.000001d;
        return Math.abs(d1 - d2) < eps;
    }
}