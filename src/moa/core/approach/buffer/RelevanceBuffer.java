package moa.core.approach.buffer;

import com.yahoo.labs.samoa.instances.Attribute;
import com.yahoo.labs.samoa.instances.Instance;
import com.yahoo.labs.samoa.instances.Instances;
import moa.classifiers.Classifier;
import moa.core.approach.util.InstanceUtils;

import java.util.*;

public class RelevanceBuffer extends Buffer {
    int rmseBufferSize = 1000;
    Deque<Double> metricBuffer;
    Random random;
    Classifier relevanceModel;
    boolean isRegressor;

    public RelevanceBuffer(int bufferSize, int attributeLength, double relevanceRatio, Random random, int[] timeIndices,
                           Classifier relevanceModel, int[] bufferIndices, boolean isRegressor) {
        this.size = bufferSize;
        this.attributeLength = attributeLength;
        this.relevanceRatio = relevanceRatio;
        this.elements = new ArrayDeque<>();
        this.metricBuffer = new ArrayDeque<>();
        this.fullSize = false;
        this.random = random;
        this.timeIndices = timeIndices;
        this.bufferIndices = bufferIndices;
        this.relevanceModel = relevanceModel;
        this.isRegressor = isRegressor;
    }

    @Override
    protected void trainRelevance(BufferElement lastElement) {
        Instance relevanceInstance = lastElement.instance.copy();
        double metric = getMetric(lastElement, isRegressor);
        metricBuffer.addFirst(metric);
        if (metricBuffer.size() > rmseBufferSize)
            metricBuffer.removeLast();
        Instance trainInstance = getRelevanceInstance(relevanceInstance, metric);

        relevanceModel.trainOnInstance(trainInstance);
    }

    private Instance getRelevanceInstance(Instance relevanceInstance, double value) {
        ArrayList<Attribute> attributes = new ArrayList<>();
        for (int i = 0; i < relevanceInstance.numAttributes(); i++) {
            Attribute att = new Attribute("Cluster" + i);
            attributes.add(att);
        }
        Attribute att = new Attribute("value");
        attributes.add(att);
        Instances format = new Instances("", attributes, 0);
        format.setClassIndex(relevanceInstance.numAttributes());
        double[] values = InstanceUtils.getValuesFromInstance(relevanceInstance);
        double[] trainValues = new double[values.length + 1];
        trainValues[values.length] = value;
        for (int i = 0; i < values.length; i++)
            trainValues[i] = values[i];
        return InstanceUtils.generateInstanceFromValues(trainValues, format);
    }

    private double getMetric(BufferElement lastElement, boolean isRegressor) {
        if(isRegressor) // RMSE
            return Math.sqrt(lastElement.squaredError / (double) lastElement.predictedEvents);
        else // accuracy
            return (double) lastElement.correctlyPredictedEvents / (double) lastElement.predictedEvents;
    }

    @Override
    protected boolean elementRelevant(BufferElement newElement) {
        Instance relevanceInstance = getRelevanceInstance(newElement.instance, 0);
        double relevancePred = Arrays.stream(relevanceModel.getVotesForInstance(relevanceInstance)).average().getAsDouble();

        double relevanceThreshold = getRelevanceThreshold();
        metricBuffer.addFirst(relevancePred);
        if (metricBuffer.size() > rmseBufferSize)
            metricBuffer.removeLast();

        if(isRegressor)
            return relevancePred <= relevanceThreshold;
        else
            return relevancePred >= relevanceThreshold;
    }

    private double getRelevanceThreshold() {
        if(isRegressor) { //Check if RMSE in bottom r percentage
            if (metricBuffer.size() == 0 || relevanceRatio == 1)
                return Double.MAX_VALUE;
            Double[] sortedBuffer = metricBuffer.stream().sorted().toArray(Double[]::new);
            int targetIndex = (int) ((double) sortedBuffer.length * relevanceRatio);
            return sortedBuffer[targetIndex];
        }
        else{ //Check if accuracy in top r percentage
            if (metricBuffer.size() == 0 || relevanceRatio == 1)
                return Double.MIN_VALUE;
            Double[] sortedBuffer = metricBuffer.stream().sorted().toArray(Double[]::new);
            int targetIndex = (int) ((double) sortedBuffer.length * (1.0 - relevanceRatio));
            return sortedBuffer[targetIndex];
        }
    }

}
