package moa.core.approach;

import com.yahoo.labs.samoa.instances.Instance;
import moa.classifiers.Classifier;
import org.apache.commons.lang3.ArrayUtils;
import scala.Tuple2;

import java.util.*;

abstract public class Buffer {
    int size;
    int attributeLength;
    double relevanceRatio;
    Deque<BufferElement> elements;
    boolean fullSize;
    int[] timeIndices;
    int[] bufferIndices;

    public void nextElement(Instance inst) {
        BufferElement bufferElem = getBufferElement(inst);
        if (!fullSize) {

            elements.addFirst(bufferElem);
            if (elements.size() == size)
                fullSize = true;
        } else {
            if (elementRelevant(bufferElem)) {
                elements.addFirst(bufferElem);
                BufferElement lastElement = elements.removeLast();
                trainRelevance(lastElement);
            }
        }
    }

    protected BufferElement getBufferElement(Instance inst) {
        Instance bufferInstance = inst.copy();
        Map<Integer, Integer> elementTimeIndices = generateElementTimeIndices(timeIndices, bufferIndices);
        if (bufferIndices[0] != -1) {
            for (int i = bufferInstance.numAttributes() - 1; i >= 0; i--)
                if (!ArrayUtils.contains(bufferIndices, i)) {
                    bufferInstance.deleteAttributeAt(i);
                }
        }
        return new BufferElement(bufferInstance, elementTimeIndices);
    }

    protected Map<Integer, Integer> generateElementTimeIndices(int[] timeIndices, int[] bufferIndices) {
        Map<Integer, Integer> ret = new HashMap<>();
        for (int index : timeIndices)
            ret.put(index, index);
        if (bufferIndices[0] == -1) return ret;
        ret = new HashMap<>();

        int[] sortedBufferIndices = Arrays.stream(bufferIndices).sorted().toArray();
        for (int timeIndex : timeIndices) {
            for (int j = 0; j < sortedBufferIndices.length; j++)
                if (sortedBufferIndices[j] == timeIndex) {
                    ret.put(timeIndex,j);
                    break;
                }
        }
        return ret;
    }

    protected abstract void trainRelevance(BufferElement lastElement);

    protected abstract boolean elementRelevant(BufferElement newElement);

    public double[][] getValues(double[] current) {
        double[][] ret = new double[size][];
//        System.out.println(elements.size());
        int index = 0;
        for (BufferElement elem : elements) {
            ret[index] = elem.values.clone();
            for (Map.Entry<Integer, Integer> timeIndex : elem.timeIndices.entrySet()) {
                ret[index][timeIndex.getValue()] -= current[timeIndex.getKey()];
            }
            index++;
        }
        for (; index < size; index++) {
            ret[index] = new double[attributeLength];
        }
        return ret;
    }

    public Instance[] getInstances(double[] current) {
        Instance[] ret = new Instance[elements.size()];
        int index = 0;
        for (BufferElement elem : elements) {
            Instance inst = elem.instance.copy();
            for (Map.Entry<Integer, Integer> timeIndex : elem.timeIndices.entrySet()) {
                inst.setValue(timeIndex.getValue(), inst.value(timeIndex.getValue()) - current[timeIndex.getKey()]);
            }

            ret[index++] = inst;
        }
        return ret;
    }

    // TODO: create a version with non-random buffer element selection
    static public Buffer getBuffer(String name, int bufferSize, int attributeLength,
                                   double relevanceRatio, Random random, int[] timeIndices, Classifier relevanceModel,
                                   int[] bufferIndices) {
        Buffer ret = null;
        switch (name) {
            case "random":
            case "random_classifier":
                ret = new RandomBuffer(bufferSize, attributeLength, relevanceRatio, random, timeIndices, bufferIndices);
                break;
            case "relevance":
                ret = new RelevanceBuffer(bufferSize, attributeLength, relevanceRatio, random, timeIndices, relevanceModel, bufferIndices);
                break;
            case "relevance_classifier":
                ret = new RelevanceClassificationBuffer(bufferSize, attributeLength, relevanceRatio, random, timeIndices, relevanceModel, bufferIndices);
                break;
        }
        return ret;
    }

    public void updateError(double prediction, double actual) {
        double err = prediction - actual;
        double squaredErr = err * err;
        boolean correct = prediction == actual;
        for (BufferElement elem : elements) {
            elem.predictedEvents += 1;
            elem.squaredError += squaredErr;
            if (correct) {
                elem.correctlyPredictedEvents += 1;
            }
        }
    }
}
