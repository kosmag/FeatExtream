package moa.core.approach;

import com.yahoo.labs.samoa.instances.Attribute;
import com.yahoo.labs.samoa.instances.Instance;
import com.yahoo.labs.samoa.instances.InstancesHeader;
import moa.cluster.Clustering;
import moa.clusterers.clustream.Clustream;

import java.util.ArrayList;

public class ClusterAggregator extends Concatenator {
    int setNumClusters;

    Clustream clusterer;
    int numClusters;

    public ClusterAggregator(int clusterNo) {
        super();
        this.setNumClusters = clusterNo;

    }

    public double[] getResult(double[] event, Buffer buffer) {
        Instance[] bufferInstances = buffer.getInstances();
        double[] bufferClusters = getClusters(bufferInstances);
        double[][] newEventArray = {event, bufferClusters};
        double[] res = EventInstance.concatenate(newEventArray);
        return res;
    }

    private double[] getClusters(Instance[] bufferInstances) {
        double[] clusterings = new double[numClusters];
        Clustering clusters = clusterer.getMicroClusteringResult();
        for (Instance inst : bufferInstances) {
            int maxClusterIndex = 0;
            double maxClusterValue = 0;
            for (int j = 0; j < clusters.size(); j++) {
                double prob = clusters.get(j).getInclusionProbability(inst);
                if (prob > maxClusterValue) {
                    maxClusterValue = prob;
                    maxClusterIndex = j;
                }
            }

            clusterings[maxClusterIndex]++;
        }
        return clusterings;
    }

    public ArrayList<Attribute> getAttributes(InstancesHeader originalHeader, int bufferSize) {

        if (clusterer == null) {
            clusterer = new Clustream();
            clusterer.maxNumKernelsOption.setValue(setNumClusters);
//            clusterer.kernelRadiFactorOption.setValue(10);
            clusterer.prepareForUse();
            clusterer.setModelContext(originalHeader);
        }

        numClusters = clusterer.maxNumKernelsOption.getValue();


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

    @Override
    public void train(Instance inst) {
        clusterer.trainOnInstance(inst);
    }
}
