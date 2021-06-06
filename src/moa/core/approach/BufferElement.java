package moa.core.approach;

import com.yahoo.labs.samoa.instances.Instance;
import scala.Int;

import java.util.Map;

public class BufferElement {
    Instance instance;
    double[] values;
    int predictedEvents;
    int correctlyPredictedEvents;
    double squaredError;
    Map<Integer, Integer> timeIndices;

    BufferElement(Instance instance, Map<Integer, Integer> timeIndices) {
        this.instance = instance;
        this.values = InstanceUtils.getValuesFromInstance(instance);
        this.predictedEvents = 0;
        this.correctlyPredictedEvents = 0;
        this.squaredError = 0;
        this.timeIndices = timeIndices;
    }
}
