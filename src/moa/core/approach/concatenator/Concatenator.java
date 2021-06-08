package moa.core.approach.concatenator;

import com.yahoo.labs.samoa.instances.Attribute;
import com.yahoo.labs.samoa.instances.InstancesHeader;
import moa.core.approach.buffer.Buffer;

import java.util.ArrayList;

abstract public class Concatenator {
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

    abstract public double[] getResult(double[] event, Buffer buffer);

    abstract public ArrayList<Attribute> getAttributes(InstancesHeader originalHeader, int bufferSize);

}
