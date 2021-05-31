package moa.core.approach;

import com.yahoo.labs.samoa.instances.Instance;

public class BufferElement {
    Instance instance;
    double[] values;
    int predictedEvents;
    int correctlyPredictedEvents;
    double squaredError;

    BufferElement(Instance instance) {
        this.instance = instance;
        this.values = InstanceUtils.getValuesFromInstance(instance);
        this.predictedEvents = 0;
        this.correctlyPredictedEvents = 0;
        this.squaredError = 0;
    }
}
