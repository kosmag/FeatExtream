package moa.core.approach;

import com.yahoo.labs.samoa.instances.Instance;
import moa.classifiers.Classifier;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.Random;

public class RelevanceBuffer extends Buffer {
    int rmseBufferSize = 1000;
    Deque<Double> rmseBuffer;
    Random random;
    Classifier relevanceModel;

    public RelevanceBuffer(int bufferSize, int attributeLength, double relevanceRatio, Random random, int[] timeIndices,
                           Classifier relevanceModel) {
        this.size = bufferSize;
        this.attributeLength = attributeLength;
        this.relevanceRatio = relevanceRatio;
        this.elements = new ArrayDeque<>();
        this.rmseBuffer = new ArrayDeque<>();
        this.fullSize = false;
        this.random = random;
        this.timeIndices = timeIndices;
        this.relevanceModel = relevanceModel;
    }

    @Override
    protected void trainRelevance(BufferElement lastElement) {
        Instance relevanceInstance = lastElement.instance.copy();
        double rmse = getRmse(lastElement);
        rmseBuffer.addFirst(rmse);
        if(rmseBuffer.size() > rmseBufferSize)
            rmseBuffer.removeLast();
        relevanceInstance.setClassValue(relevanceInstance.classIndex(), rmse);
        relevanceModel.trainOnInstance(relevanceInstance);

    }

    private double getRmse(BufferElement lastElement) {
        return Math.sqrt(lastElement.squaredError / (double) lastElement.predictedEvents);
    }

    @Override
    protected boolean elementRelevant(BufferElement newElement) {
        double relevancePred = Arrays.stream(relevanceModel.getVotesForInstance(newElement.instance)).average().getAsDouble();

        double relevanceThreshold = getRelevanceThreshold();
        rmseBuffer.addFirst(relevancePred);
        if(rmseBuffer.size() > rmseBufferSize)
            rmseBuffer.removeLast();

        return relevancePred <= relevanceThreshold;
    }

    private double getRelevanceThreshold() {
        if(rmseBuffer.size() == 0 || relevanceRatio == 1)
            return Double.MAX_VALUE;
        Double[] sortedBuffer = rmseBuffer.stream().sorted().toArray(Double[]::new);
        int targetIndex = (int)((double)sortedBuffer.length * relevanceRatio);
        return sortedBuffer[targetIndex];

    }

}
