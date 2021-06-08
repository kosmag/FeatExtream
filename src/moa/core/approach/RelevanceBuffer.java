package moa.core.approach;

import com.yahoo.labs.samoa.instances.*;
import moa.classifiers.Classifier;

import java.util.*;

public class RelevanceBuffer extends Buffer {
    int rmseBufferSize = 1000;
    Deque<Double> rmseBuffer;
    Random random;
    Classifier relevanceModel;

    public RelevanceBuffer(int bufferSize, int attributeLength, double relevanceRatio, Random random, int[] timeIndices,
                           Classifier relevanceModel, int[] bufferIndices) {
        this.size = bufferSize;
        this.attributeLength = attributeLength;
        this.relevanceRatio = relevanceRatio;
        this.elements = new ArrayDeque<>();
        this.rmseBuffer = new ArrayDeque<>();
        this.fullSize = false;
        this.random = random;
        this.timeIndices = timeIndices;
        this.bufferIndices = bufferIndices;
        this.relevanceModel = relevanceModel;
    }

    @Override
    protected void trainRelevance(BufferElement lastElement) {
        Instance relevanceInstance = lastElement.instance.copy();
        double rmse = getRmse(lastElement);
        rmseBuffer.addFirst(rmse);
        if (rmseBuffer.size() > rmseBufferSize)
            rmseBuffer.removeLast();
        Instance trainInstance = getRelevanceInstance(relevanceInstance,rmse);

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
        Instances format = new Instances("",attributes, 0);
        format.setClassIndex(relevanceInstance.numAttributes());
        double[] values = InstanceUtils.getValuesFromInstance(relevanceInstance);
        double[] trainValues = new double[values.length + 1];
        trainValues[values.length] = value;
        for(int i = 0; i< values.length; i++)
            trainValues[i] = values[i];
        return InstanceUtils.generateInstanceFromValues(trainValues,format);
    }

    private double getRmse(BufferElement lastElement) {
        return Math.sqrt(lastElement.squaredError / (double) lastElement.predictedEvents);
    }

    @Override
    protected boolean elementRelevant(BufferElement newElement) {
        Instance relevanceInstance = getRelevanceInstance(newElement.instance,0);
        double relevancePred = Arrays.stream(relevanceModel.getVotesForInstance(relevanceInstance)).average().getAsDouble();

        double relevanceThreshold = getRelevanceThreshold();
        rmseBuffer.addFirst(relevancePred);
        if (rmseBuffer.size() > rmseBufferSize)
            rmseBuffer.removeLast();

        return relevancePred <= relevanceThreshold;
    }

    private double getRelevanceThreshold() {
        if (rmseBuffer.size() == 0 || relevanceRatio == 1)
            return Double.MAX_VALUE;
        Double[] sortedBuffer = rmseBuffer.stream().sorted().toArray(Double[]::new);
        int targetIndex = (int) ((double) sortedBuffer.length * relevanceRatio);
        return sortedBuffer[targetIndex];

    }

}
