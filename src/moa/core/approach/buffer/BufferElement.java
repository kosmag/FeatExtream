package moa.core.approach.buffer;

import com.yahoo.labs.samoa.instances.Instance;
import moa.core.approach.util.InstanceUtils;

import java.util.Map;

public class BufferElement {
    Instance instance;
    double[] values;
    int predictedEvents;
    int correctlyPredictedEvents;
    double squaredError;
    Map<Integer, Integer> timeIndices;
    public boolean trainedCluster;

    BufferElement(Instance instance, Map<Integer, Integer> timeIndices) {
        this.instance = instance;
        this.values = InstanceUtils.getValuesFromInstance(instance);
        this.predictedEvents = 0;
        this.correctlyPredictedEvents = 0;
        this.squaredError = 0;
        this.timeIndices = timeIndices;
        this.trainedCluster = false;
    }
}
