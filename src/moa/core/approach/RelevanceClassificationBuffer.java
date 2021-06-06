package moa.core.approach;

import com.yahoo.labs.samoa.instances.Instance;
import moa.classifiers.Classifier;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.Random;

public class RelevanceClassificationBuffer extends Buffer {
    int rmseBufferSize = 1000;
    Deque<Double> accuracyBuffer;
    Random random;
    Classifier relevanceModel;

    public RelevanceClassificationBuffer(int bufferSize, int attributeLength, double relevanceRatio, Random random, int[] timeIndices,
                                         Classifier relevanceModel, int[] bufferIndices) {
        this.size = bufferSize;
        this.attributeLength = attributeLength;
        this.relevanceRatio = relevanceRatio;
        this.elements = new ArrayDeque<>();
        this.accuracyBuffer = new ArrayDeque<>();
        this.fullSize = false;
        this.random = random;
        this.timeIndices = timeIndices;
        this.bufferIndices = bufferIndices;
        this.relevanceModel = relevanceModel;
    }

    @Override
    protected void trainRelevance(BufferElement lastElement) {
        Instance relevanceInstance = lastElement.instance.copy();
        double accuracy = getAccuracy(lastElement);
        accuracyBuffer.addFirst(accuracy);
        if (accuracyBuffer.size() > rmseBufferSize)
            accuracyBuffer.removeLast();
        relevanceInstance.setClassValue(relevanceInstance.classIndex(), accuracy);
        relevanceModel.trainOnInstance(relevanceInstance);

    }

    private double getAccuracy(BufferElement lastElement) {
        return (double) lastElement.correctlyPredictedEvents / (double) lastElement.predictedEvents;
    }

    @Override
    protected boolean elementRelevant(BufferElement newElement) {
        double relevancePred = Arrays.stream(relevanceModel.getVotesForInstance(newElement.instance)).average().getAsDouble();

        double relevanceThreshold = getRelevanceThreshold();
        accuracyBuffer.addFirst(relevancePred);
        if (accuracyBuffer.size() > rmseBufferSize)
            accuracyBuffer.removeLast();

        return relevancePred >= relevanceThreshold;
    }

    private double getRelevanceThreshold() {
        if (accuracyBuffer.size() == 0 || relevanceRatio == 1)
            return Double.MIN_VALUE;
        Double[] sortedBuffer = accuracyBuffer.stream().sorted().toArray(Double[]::new);
        int targetIndex = (int) ((double) sortedBuffer.length * relevanceRatio);
        return sortedBuffer[targetIndex];

    }

}
