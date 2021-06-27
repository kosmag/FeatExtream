package moa.core.approach;

import com.yahoo.labs.samoa.instances.Instance;

public class ArrivedEvent {
    public Instance instance;
    double firstPrediction;

    public ArrivedEvent(Instance inst) {
        this.instance = inst;
    }

    public void addPrediction(double prediction) {
        firstPrediction = prediction;
    }
}
