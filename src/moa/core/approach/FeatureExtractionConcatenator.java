package moa.core.approach;

import com.yahoo.labs.samoa.instances.Attribute;
import com.yahoo.labs.samoa.instances.Instance;
import com.yahoo.labs.samoa.instances.Instances;
import com.yahoo.labs.samoa.instances.InstancesHeader;
import moa.cluster.Cluster;
import moa.cluster.Clustering;
import moa.clusterers.CobWeb;
import moa.clusterers.clustream.Clustream;

import java.util.ArrayList;

public class FeatureExtractionConcatenator extends Concatenator {

    Clustream clusterer;
    int numClusters;

    public FeatureExtractionConcatenator() {
        super();
    }

    public double[] getResult(double[] event, Buffer buffer) {
        Instance[] bufferInstances = buffer.getInstances();
        int maxBufferSize = buffer.size;
        double[] bufferClusters = getClusters(bufferInstances, maxBufferSize);
        double[][] newEventArray = {event, bufferClusters};
        double[] res = EventInstance.concatenate(newEventArray);
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

    @Override
    public void train(Instance inst) {
        clusterer.trainOnInstance(inst);
    }
}