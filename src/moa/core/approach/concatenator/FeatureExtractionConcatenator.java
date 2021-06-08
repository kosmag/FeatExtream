package moa.core.approach.concatenator;

import com.yahoo.labs.samoa.instances.Attribute;
import com.yahoo.labs.samoa.instances.Instance;
import com.yahoo.labs.samoa.instances.InstancesHeader;
import moa.cluster.Clustering;
import moa.clusterers.clustream.Clustream;
import moa.core.approach.buffer.Buffer;
import moa.core.approach.clusterer.ClusterHelper;
import moa.core.approach.util.InstanceUtils;

import java.util.ArrayList;

public class FeatureExtractionConcatenator extends FeatureExtractor {

    ClusterHelper clusterer;
    int numClusters;

    public FeatureExtractionConcatenator(String clusterType, int numClusters) {
        super();
        this.numClusters = numClusters;
        this.clusterer = new ClusterHelper(clusterType, numClusters);
    }

    public double[] getResult(double[] event, Buffer buffer) {
        Instance[] bufferInstances = buffer.getInstances(event);
        int maxBufferSize = buffer.size;
        double[] bufferClusters = clusterer.getConcatenatedClusters(bufferInstances, maxBufferSize);
        double[][] newEventArray = {event, bufferClusters};
        double[] res = InstanceUtils.concatenate(newEventArray);
        for (Instance inst : bufferInstances)
            clusterer.train(inst);
        return res;
    }


    public ArrayList<Attribute> getAttributes(InstancesHeader originalHeader, int bufferSize) {

        if (!clusterer.isSetUp) {
            clusterer.setup(originalHeader);
        }

        ArrayList<Attribute> attributes = new ArrayList<>();

        for (int i = 0; i < originalHeader.numAttributes(); i++) {
            Attribute att = originalHeader.attribute(i);
            attributes.add(att);
        }
        for (int i = 0; i < bufferSize; i++) {
            Attribute att = new Attribute("Cluster" + i);
            attributes.add(att);
        }
        return attributes;
    }


}
