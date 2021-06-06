package moa.core.approach;

import com.yahoo.labs.samoa.instances.Attribute;
import com.yahoo.labs.samoa.instances.Instance;
import com.yahoo.labs.samoa.instances.Instances;
import com.yahoo.labs.samoa.instances.InstancesHeader;
import moa.streams.InstanceStream;

import java.util.ArrayList;

abstract public class Concatenator {
    abstract public double[] getResult(double[] event, Buffer buffer);

    abstract public ArrayList<Attribute> getAttributes(InstancesHeader originalHeader, int bufferSize);

    static public Concatenator getConcatenator(String name, int clusterNo) {
        Concatenator ret = null;
        switch (name) {
            case "naive":
                ret = new NaiveConcatenator();
                break;
            case "featureExtraction":
                ret = new FeatureExtractionConcatenator(clusterNo);
                break;
            case "cluster":
                ret = new ClusterAggregator(clusterNo);
                break;
            case "cep":
                ret = new CEPAggregator(clusterNo);
                break;

        }
        return ret;
    }

    public abstract void train(Instance inst);
}
