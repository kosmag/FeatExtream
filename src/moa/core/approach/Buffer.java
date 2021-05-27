package moa.core.approach;

import java.util.Deque;
import java.util.Random;

abstract public class Buffer {
    int size;
    int attributeLength;
    double relevanceRatio;
    Deque<BufferElement> elements;
    boolean fullSize;
    int[] timeIndices;

    public void nextElement(double[] values) {
        if (!fullSize) {
            elements.addFirst(new BufferElement(values));
            if (elements.size() == size)
                fullSize = true;
        } else {
            BufferElement newElement = new BufferElement(values);
            if (elementRelevant(newElement)) {
                elements.addFirst(newElement);
                BufferElement lastElement = elements.removeLast();
                trainRelevance(lastElement);
            }
        }
    }

    protected abstract void trainRelevance(BufferElement lastElement);

    protected abstract boolean elementRelevant(BufferElement newElement);

    public double[][] getValues(double[] current) {
        double[][] ret = new double[size][];
//        System.out.println(elements.size());
        int index = 0;
        for (BufferElement elem : elements) {
            ret[index] = elem.value.clone();
            for(int timeIndex: timeIndices){
                ret[index][timeIndex] -= current[timeIndex];
            }
            index++;
        }
        for(;index < size; index++){
            ret[index] = new double[attributeLength];
        }
        return ret;
    }

    // TODO: create a version with non-random buffer element selection
    static public Buffer getBuffer(String name, int bufferSize, int attributeLength,
                                   double relevanceRatio, Random random, int[] timeIndices) {
        Buffer ret = null;
        switch (name) {
            case "random":
                ret = new RandomBuffer(bufferSize, attributeLength, relevanceRatio, random,timeIndices);
        }
        return ret;
    }

    public void updateError(double prediction, double actual){
        double err = prediction - actual;
        double squaredErr = err * err;
        for (BufferElement elem : elements) {
            elem.predictedEvents += 1;
            elem.squaredError += squaredErr;
        }
    }
}
