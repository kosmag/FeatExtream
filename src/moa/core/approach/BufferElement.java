package moa.core.approach;

public class BufferElement {
    double[] value;
    int predictedEvents;
    double squaredError;

    BufferElement(double[] value) {
        this.value = value;
        this.predictedEvents = 0;
        this.squaredError = 0;
    }
}
