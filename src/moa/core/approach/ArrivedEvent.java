package moa.core.approach;

import com.yahoo.labs.samoa.instances.Instance;

import java.util.ArrayList;
import java.util.List;

public class ArrivedEvent {
    public Instance instance;
    int reevalFrequency;
    int totalBinNo;
    double firstPrediction;
    List<Double> iterPrediction;
    double lastPrediction;
    public ArrivedEvent(Instance inst, int reevalFrequency, int totalBinNo) {
        this.instance = inst;
        this.reevalFrequency = reevalFrequency;
        this.totalBinNo = totalBinNo;
        iterPrediction = new ArrayList<>();
    }

    public void addPrediction(double prediction, int binNo) {
        if (binNo == 0){
            firstPrediction = prediction;
        }
        else if(binNo == totalBinNo + 1){
            lastPrediction = prediction;
        }
        else iterPrediction.add(prediction);
    }
}
