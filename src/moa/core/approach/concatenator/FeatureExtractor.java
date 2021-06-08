package moa.core.approach.concatenator;

import com.yahoo.labs.samoa.instances.Attribute;
import com.yahoo.labs.samoa.instances.InstancesHeader;
import moa.core.approach.buffer.Buffer;

import java.util.ArrayList;

abstract public class FeatureExtractor {
    static public FeatureExtractor getConcatenator(String name, int clusterNo, String clusterType) {
        FeatureExtractor ret = null;
        switch (name) {
            case "naive":
                ret = new NaiveConcatenator();
                break;
            case "featureExtraction":
                ret = new FeatureExtractionConcatenator(clusterType, clusterNo);
                break;
            case "cluster":
                ret = new ClusterAggregator(clusterType, clusterNo);
                break;
            case "cep":
                ret = new CEPAggregator(clusterType, clusterNo);
                break;

        }
        return ret;
    }

    abstract public double[] getResult(double[] event, Buffer buffer);

    abstract public ArrayList<Attribute> getAttributes(InstancesHeader originalHeader, int bufferSize);

}
