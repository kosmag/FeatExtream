package moa.core.approach.concatenator;

import com.yahoo.labs.samoa.instances.Attribute;
import com.yahoo.labs.samoa.instances.Instance;
import com.yahoo.labs.samoa.instances.InstancesHeader;
import moa.cluster.Clustering;
import moa.clusterers.clustream.Clustream;
import moa.core.approach.buffer.Buffer;
import moa.core.approach.util.InstanceUtils;

import java.util.ArrayList;

public class FeatureExtractionConcatenator extends Concatenator {

    Clustream clusterer;
    int numClusters;
    int setNumClusters;

    public FeatureExtractionConcatenator(int clusterNo) {
        super();
        this.setNumClusters = clusterNo;
    }

    public double[] getResult(double[] event, Buffer buffer) {
        Instance[] bufferInstances = buffer.getInstances(event);
        int maxBufferSize = buffer.size;
        double[] bufferClusters = getClusters(bufferInstances, maxBufferSize);
        double[][] newEventArray = {event, bufferClusters};
        double[] res = InstanceUtils.concatenate(newEventArray);
        return res;
    }

    private double[] getClusters(Instance[] bufferInstances, int maxBufferSize) {
        double[] clusterings = new double[maxBufferSize];
        Clustering clusters = clusterer.getMicroClusteringResult();
        int index = 0;
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

            clusterings[index] = maxClusterIndex;
            index++;
        }
        return clusterings;
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
        for (int i = 0; i < bufferSize; i++) {
            Attribute att = new Attribute("Cluster" + i);
            attributes.add(att);
        }
        return attributes;
    }


}
