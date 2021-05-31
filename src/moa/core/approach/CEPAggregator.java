package moa.core.approach;

import com.yahoo.labs.samoa.instances.Attribute;
import com.yahoo.labs.samoa.instances.Instance;
import com.yahoo.labs.samoa.instances.InstancesHeader;
import moa.cluster.Clustering;
import moa.clusterers.clustream.Clustream;

import java.util.ArrayList;

public class CEPAggregator extends Concatenator {

    Clustream clusterer;
    int numClusters;

    public CEPAggregator() {
        super();
    }

    public double[] getResult(double[] event, Buffer buffer) {
        Instance[] bufferInstances = buffer.getInstances();
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
                if (c2 > c1) c2 = c2 - 1;
                cepClusterings[c1][c2]++;
            }
        }
        return EventInstance.concatenate(cepClusterings);
    }

    private double[][] initClusterings() {
        double[][] ret = new double[numClusters][];
        for (int i = 0; i < numClusters; i++) {
            ret[i] = new double[numClusters - 1];
            for (int j = 0; j < numClusters - 1; j++)
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
            clusterer.maxNumKernelsOption.setValue(8);
            clusterer.prepareForUse();
            clusterer.setModelContext(originalHeader);
        }

        numClusters = clusterer.maxNumKernelsOption.getValue();


        ArrayList<Attribute> attributes = new ArrayList<>();

        for (int i = 0; i < originalHeader.numAttributes(); i++) {
            Attribute att = originalHeader.attribute(i);
            attributes.add(att);
        }
        for (int i = 0; i < numClusters * (numClusters - 1); i++) {
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
