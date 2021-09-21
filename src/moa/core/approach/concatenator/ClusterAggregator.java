package moa.core.approach.concatenator;

import com.yahoo.labs.samoa.instances.Attribute;
import com.yahoo.labs.samoa.instances.Instance;
import com.yahoo.labs.samoa.instances.InstancesHeader;
import moa.core.approach.buffer.Buffer;
import moa.core.approach.buffer.BufferElement;
import moa.core.approach.clusterer.ClusterHelper;
import moa.core.approach.util.InstanceUtils;

import java.util.ArrayList;

public class ClusterAggregator extends FeatureExtractor {

    ClusterHelper clusterer;
    int numClusters;

    public ClusterAggregator(String clusterType, int numClusters) {
        super();
        this.numClusters = numClusters;
        this.clusterer = new ClusterHelper(clusterType, numClusters);

    }
    int  counter = 0;
    public double[] getResult(double[] event, Buffer buffer) {
        Instance[] bufferInstances = buffer.getInstances(event);
        double[] bufferClusters = clusterer.getClusters(bufferInstances);
        double[][] newEventArray = {event, bufferClusters};
        double[] res = InstanceUtils.concatenate(newEventArray);
        int index = 0;
        for (BufferElement elem : buffer.elements) {
            if (!elem.trainedCluster) {
                counter++;
                clusterer.train(bufferInstances[index]);
                elem.trainedCluster = true;
            }
            index++;
        }
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
        for (int i = 0; i < numClusters; i++) {
            Attribute att = new Attribute("Cluster" + i);
            attributes.add(att);
        }
        return attributes;
    }

}
