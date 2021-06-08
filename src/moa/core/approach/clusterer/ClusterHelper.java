package moa.core.approach.clusterer;

import com.yahoo.labs.samoa.instances.Instance;
import com.yahoo.labs.samoa.instances.InstancesHeader;
import moa.cluster.Clustering;
import moa.clusterers.AbstractClusterer;
import moa.clusterers.clustream.Clustream;
import moa.clusterers.clustree.ClusTree;
import moa.core.approach.util.InstanceUtils;

public class ClusterHelper {

    AbstractClusterer clusterer;
    int numClusters;
    String type;

    public boolean isSetUp;

    public ClusterHelper(String type, int numClusters) {
        this.type = type;
        this.numClusters = numClusters;
        this.isSetUp = false;
    }

    public void setup(InstancesHeader originalHeader) {
        switch (type) {
            case "clustream":
                clusterer = new Clustream();
                ((Clustream) clusterer).maxNumKernelsOption.setValue(numClusters);
                break;
            case "clustree":
                clusterer = new ClusTree();
                break;
        }
        clusterer.prepareForUse();
        clusterer.setModelContext(originalHeader);
        isSetUp = true;
    }

    public double[] getClusters(Instance[] bufferInstances) {
        double[] clusterings = new double[numClusters];
        Clustering clusters = clusterer.getMicroClusteringResult();
        if (clusters == null)
            return clusterings;
        for (Instance inst : bufferInstances) {
            int maxClusterIndex = getMaxClusterIndex(inst, clusters);
            if (maxClusterIndex != -1)
                clusterings[maxClusterIndex % numClusters]++;
        }
        return clusterings;
    }


    public double[] getCEPClusters(Instance[] bufferInstances) {
        Clustering clusters = clusterer.getMicroClusteringResult();
        int[] clusterValues = new int[bufferInstances.length];
        for (int i = 0; i < bufferInstances.length; i++) {
            clusterValues[i] = getMaxClusterIndex(bufferInstances[i], clusters);
        }
        double[][] cepClusterings = initClusterings();

        for (int i = 0; i < bufferInstances.length; i++) {
            int c1 = clusterValues[i];
            if (c1 != -1)
                for (int j = i + 1; j < bufferInstances.length; j++) {
                    int c2 = clusterValues[j];
                    if(c2 != -1)
                        cepClusterings[c1][c2]++;
            }
        }
        return InstanceUtils.concatenate(cepClusterings);
    }

    public double[] getConcatenatedClusters(Instance[] bufferInstances, int maxBufferSize) {
        double[] clusterings = new double[maxBufferSize];
        Clustering clusters = clusterer.getMicroClusteringResult();
        for (int i = 0; i < bufferInstances.length; i++) {
            clusterings[i] = getMaxClusterIndex(bufferInstances[0], clusters);
        }
        return clusterings;
    }

    private int getMaxClusterIndex(Instance inst, Clustering clustering) {
        int maxClusterIndex = -1;
        double maxClusterValue = 0;
        for (int j = 0; j < clustering.size(); j++) {
            double prob = clustering.get(j).getInclusionProbability(inst);
            if (prob > maxClusterValue) {
                maxClusterValue = prob;
                maxClusterIndex = j;
            }
        }
        return maxClusterIndex;
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


    public void train(Instance inst) {
        clusterer.trainOnInstance(inst);
    }
}
