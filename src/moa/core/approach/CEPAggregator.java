package moa.core.approach;

import com.yahoo.labs.samoa.instances.Attribute;
import com.yahoo.labs.samoa.instances.Instance;
import com.yahoo.labs.samoa.instances.InstancesHeader;
import moa.cluster.Clustering;
import moa.clusterers.clustream.Clustream;

import java.util.ArrayList;

public class CEPAggregator extends Concatenator {
    int setNumClusters;
    Clustream clusterer;
    int numClusters;

    public CEPAggregator(int clusterNo) {
        super();
        this.setNumClusters = clusterNo;

    }

    public double[] getResult(double[] event, Buffer buffer) {
        Instance[] bufferInstances = buffer.getInstances(event);
        double[] bufferClusters = getClusters(bufferInstances);
        double[][] newEventArray = {event, bufferClusters};
        double[] res = EventInstance.concatenate(newEventArray);
        return res;
    }

    private double[] getClusters(Instance[] bufferInstances) {
        Clustering clusters = clusterer.getMicroClusteringResult();
        int[] clusterValues = new int[bufferInstances.length];
        for (int i = 0; i < bufferInstances.length; i++) {
            clusterValues[i] = getCluster(bufferInstances[i], clusters);
        }
        double[][] cepClusterings = initClusterings();

        for (int i = 0; i < bufferInstances.length; i++) {
            int c1 = clusterValues[i];
            for (int j = i + 1; j < bufferInstances.length; j++) {
                int c2 = clusterValues[j];
                cepClusterings[c1][c2]++;
            }
        }
        return EventInstance.concatenate(cepClusterings);
    }

    private double[][] initClusterings() {
        double[][] ret = new double[numClusters][];
        for (int i = 0; i < numClusters; i++) {
            ret[i] = new double[numClusters];
            for (int j = 0; j < numClusters; j++)
                ret[i][j] = 0;
        }
        return ret;
    }

    public int getCluster(Instance inst, Clustering clusters) {
        int maxClusterIndex = 0;
        double maxClusterValue = 0;
        for (int j = 0; j < clusters.size(); j++) {
            double prob = clusters.get(j).getInclusionProbability(inst);
            if (prob > maxClusterValue) {
                maxClusterValue = prob;
                maxClusterIndex = j;
            }
        }
        return maxClusterIndex;
    }

    public ArrayList<Attribute> getAttributes(InstancesHeader originalHeader, int bufferSize) {

        if (clusterer == null) {
            clusterer = new Clustream();
            clusterer.maxNumKernelsOption.setValue(setNumClusters);
            clusterer.prepareForUse();
            clusterer.setModelContext(originalHeader);
        }

        numClusters = clusterer.maxNumKernelsOption.getValue();


        ArrayList<Attribute> attributes = new ArrayList<>();

        for (int i = 0; i < originalHeader.numAttributes(); i++) {
            Attribute att = originalHeader.attribute(i);
            attributes.add(att);
        }
        for (int i = 0; i < numClusters * numClusters; i++) {
            Attribute att = new Attribute("Cluster" + i);
            attributes.add(att);
        }
        return attributes;
    }


}
