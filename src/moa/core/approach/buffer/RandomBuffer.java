package moa.core.approach.buffer;

import java.util.ArrayDeque;
import java.util.Random;

public class RandomBuffer extends Buffer {
    Random random;

    public RandomBuffer(int bufferSize, int attributeLength, double relevanceRatio, Random random, int[] timeIndices, int[] bufferIndices) {
        this.size = bufferSize;
        this.attributeLength = attributeLength;
        this.relevanceRatio = relevanceRatio;
        this.elements = new ArrayDeque<>();
        this.fullSize = false;
        this.random = random;
        this.timeIndices = timeIndices;
        this.bufferIndices = bufferIndices;
    }

    @Override
    protected void trainRelevance(BufferElement lastElement) {
        return;
    }

    @Override
    protected boolean elementRelevant(BufferElement newElement) {
        return random.nextDouble() <= this.relevanceRatio;
    }

}
