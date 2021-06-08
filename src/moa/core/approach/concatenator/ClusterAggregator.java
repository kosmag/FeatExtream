package moa.core.approach.concatenator;

import com.yahoo.labs.samoa.instances.Attribute;
import com.yahoo.labs.samoa.instances.Instance;
import com.yahoo.labs.samoa.instances.InstancesHeader;
import moa.cluster.Clustering;
import moa.clusterers.clustree.ClusTree;
import moa.core.approach.buffer.Buffer;

import java.util.ArrayList;

public class ClusterAggregator extends Concatenator {
    int setNumClusters;

    //    Clustream clusterer;
    ClusTree clusterer;
    int numClusters;

    public ClusterAggregator(int clusterNo) {
        super();
        this.setNumClusters = clusterNo;

    }

    public double[] getResult(double[] event, Buffer buffer) {
        Instance[] bufferInstances = buffer.getInstances(event);
        double[] bufferClusters = getClusters(bufferInstances);
        double[][] newEventArray = {event, bufferClusters};
        double[] res = EventInstance.concatenate(newEventArray);
        for (Instance inst : bufferInstances)
            clusterer.trainOnInstance(inst);
        return res;
    }

    private double[] getClusters(Instance[] bufferInstances) {
        double[] clusterings = new double[numClusters];
        Clustering clusters = clusterer.getMicroClusteringResult();
        if (clusters == null)
            return clusterings;
        for (Instance inst : bufferInstances) {
            int maxClusterIndex = -1;
            double maxClusterValue = 0;
            for (int j = 0; j < clusters.size(); j++) {
                double prob = clusters.get(j).getInclusionProbability(inst);
                if (prob > maxClusterValue) {
                    maxClusterValue = prob;
                    maxClusterIndex = j;
                }
            }
            if (maxClusterIndex != -1)
                clusterings[maxClusterIndex % numClusters]++;
        }
        return clusterings;
    }

    public ArrayList<Attribute> getAttributes(InstancesHeader originalHeader, int bufferSize) {

        if (clusterer == null) {
//            clusterer = new Clustream();
            clusterer = new ClusTree();
//            clusterer.maxNumKernelsOption.setValue(setNumClusters);
//            clusterer.maxHeightOption.setValue(15);
//            clusterer.kernelRadiFactorOption.setValue(10);
            clusterer.prepareForUse();
            clusterer.setModelContext(originalHeader);
        }

//        numClusters = clusterer.maxNumKernelsOption.getValue();
        numClusters = setNumClusters;

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
