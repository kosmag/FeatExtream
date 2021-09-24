/*
 *    BufferLearner.java
 *    Copyright (C) 2021 Warsaw University of Technology, Warsaw, Poland
 *    @author Kosma Grochowski
 *
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package moa.classifiers;

import com.github.javacliparser.FloatOption;
import com.github.javacliparser.IntOption;
import com.github.javacliparser.StringOption;
import com.yahoo.labs.samoa.instances.Attribute;
import com.yahoo.labs.samoa.instances.Instance;
import com.yahoo.labs.samoa.instances.Instances;
import com.yahoo.labs.samoa.instances.InstancesHeader;
import moa.capabilities.CapabilitiesHandler;
import moa.capabilities.Capability;
import moa.capabilities.ImmutableCapabilities;
import moa.core.Measurement;
import moa.core.ObjectRepository;
import moa.core.Utils;
import moa.core.approach.ArrivedEvent;
import moa.core.approach.buffer.Buffer;
import moa.core.approach.concatenator.FeatureExtractor;
import moa.core.approach.util.InstanceUtils;
import moa.options.ClassOption;
import moa.streams.InstanceStream;
import moa.tasks.TaskMonitor;

import java.util.*;

public class BufferLearner extends AbstractClassifier implements MultiClassClassifier,
        CapabilitiesHandler, Regressor {
    /**
     * Classification and regression semi-supervised learner that buffers incoming events to extract features from them
     *
     * @author Kosma Grochowski
     * @version $Revision: 1 $
     *
     *
     */
    private static final long serialVersionUID = 1L;
    public ClassOption learnerOpt = new ClassOption("learner", 'l',
            "Learner to train.", Classifier.class,
            "moa.classifiers.meta.AdaptiveRandomForest");
    public IntOption bufferSizeOpt = new IntOption("bufferSize", 'n',
            "Buffer size", 10, 1, Integer.MAX_VALUE);
    public FloatOption relevanceRatioOpt = new FloatOption("relevanceRatio", 'r',
            "Relevance ratio", 1, 0.001, 1);
    public ClassOption relevanceModelOpt = new ClassOption("relevanceModel", 'm',
            "Relevance model", Classifier.class,
            "moa.classifiers.meta.AdaptiveRandomForestRegressor");
    public StringOption concatenatorOpt = new StringOption("concatenator", 'c',
            "Concatenator", "naive");
    public StringOption bufferOpt = new StringOption("buffer", 'b',
            "Buffer", "random");
    public IntOption partitionIndexOpt = new IntOption("partitionIndex", 'p',
            "Partition index", 0, -1, Integer.MAX_VALUE);
    public StringOption timeIndicesOpt = new StringOption("timeIndices", 't',
            "Time indices, comma separated", "");
    public StringOption bufferIndicesOpt = new StringOption("bufferIndices", 'y',
            "Buffer attribute indices, comma separated, -1 for all", "-1");
    public IntOption clusterNoOpt = new IntOption("clusterNo", 'x',
            "Cluster number", 5, 1, Integer.MAX_VALUE);

    public StringOption clusterTypeOpt = new StringOption("clusterType", 'q',
            "Cluster type", "clustree");
    public Classifier learner;
    public Classifier relevanceModel;
    protected InstancesHeader newHeader;
    private int bufferSize;
    private int partitionIndex;
    private int[] timeIndices;
    private int[] bufferIndices;
    private int clusterNo;
    private double relevanceRatio;
    private FeatureExtractor featureExtractor;
    private Map<Integer, Buffer> buffer;
    public BufferLearner() {
    }

    public BufferLearner(String learnerCLI, int bufferSize, double relevanceRatio, String relevanceModelCLI, int randomSeed,
                         String concatenator, String buffer, int partitionIndex, String timeIndices, String bufferIndices,
                         int clusterNo, String clusterType) {
        this.learnerOpt.setValueViaCLIString(learnerCLI);
        this.bufferSizeOpt.setValue(bufferSize);
        this.randomSeedOption.setValue(randomSeed);
        this.relevanceRatioOpt.setValue(relevanceRatio);
        this.relevanceModelOpt.setValueViaCLIString(relevanceModelCLI);
        this.concatenatorOpt.setValue(concatenator);
        this.bufferOpt.setValue(buffer);
        this.partitionIndexOpt.setValue(partitionIndex);
        this.timeIndicesOpt.setValue(timeIndices);
        this.bufferIndicesOpt.setValue(bufferIndices);
        this.clusterNoOpt.setValue(clusterNo);
        this.clusterTypeOpt.setValue(clusterType);
    }

    @Override
    public String getPurposeString() {
        return "Buffer learner";
    }

    @Override
    public void prepareForUseImpl(TaskMonitor monitor, ObjectRepository repository) {
        this.randomSeed = this.randomSeedOption.getValue();
        this.classifierRandom = new Random(this.randomSeed);

        create();
        learner.prepareForUse(monitor, repository);
        relevanceModel.prepareForUse(monitor, repository);
    }

    private void create() {
        this.learner = (Classifier) getPreparedClassOption(learnerOpt);
        this.bufferSize = bufferSizeOpt.getValue();
        this.relevanceRatio = relevanceRatioOpt.getValue();
        this.relevanceModel = (Classifier) getPreparedClassOption(relevanceModelOpt);
        this.clusterNo = clusterNoOpt.getValue();
        this.featureExtractor = FeatureExtractor.getConcatenator(concatenatorOpt.getValue(), this.clusterNo, clusterTypeOpt.getValue());
        this.buffer = new HashMap<>();
        this.partitionIndex = partitionIndexOpt.getValue();
        this.timeIndices = getTimeIndices(timeIndicesOpt.getValue());
        this.bufferIndices = getTimeIndices(bufferIndicesOpt.getValue());
    }

    private int[] getTimeIndices(String value) {
        if (value.equals("")) return new int[0];
        String[] strIndices = value.split(",");
        int[] ret = new int[strIndices.length];
        for (int i = 0; i < strIndices.length; i++) {
            ret[i] = Integer.parseInt(strIndices[i]);
        }
        return ret;
    }

    @Override
    public boolean isRandomizable() {
        return true;
    }

    @Override
    public double[] getVotesForInstance(Instance inst) {

        Instance extractedInstance = extractInstance(InstanceUtils.getValuesFromInstance(inst), buffer.get(startAndGetPartition(inst)));
        return this.learner.getVotesForInstance(extractedInstance);
    }

    @Override
    public void resetLearningImpl() {
        this.learner = ((Classifier) getPreparedClassOption(this.learnerOpt)).copy();
        this.learner.resetLearning();
        this.relevanceModel = ((Classifier) getPreparedClassOption(this.relevanceModelOpt)).copy();
        this.relevanceModel.resetLearning();
    }


    @Override
    public void trainOnInstanceImpl(Instance inst) {
        boolean labeled = isLabeled(inst);
        boolean updatable = true;



        updateBufferAndTrain(inst, labeled, updatable);


    }

    private double getResult(Instance instance) {
        if (learner instanceof Regressor) {
            OptionalDouble result =Arrays.stream(learner.getVotesForInstance(instance)).average();
            if (result.isPresent())
                return result.getAsDouble();
            else
                return 0;
        }
        else
            return Utils.maxIndex(getVotesForInstance(instance));
    }



    private void updateBufferAndTrain(Instance inst, boolean labeled, boolean updatable) {
        int partition = startAndGetPartition(inst);

        Buffer partitionBuffer = buffer.get(partition);

        double[] originalValues = InstanceUtils.getValuesFromInstance(inst);
        if (labeled) {
            Instance extractedInstance = extractInstance(originalValues, partitionBuffer);

            checkIntegrity(inst, extractedInstance);

            double prediction = getResult(extractedInstance);
            partitionBuffer.updateError(prediction, inst.classValue());

            this.learner.trainOnInstance(extractedInstance);
        }
        if (updatable) {
            partitionBuffer.nextElement(inst);
        }
    }

    private boolean isLabeled(Instance inst) {
        boolean labeled = true;
        for (int m = 0; m < inst.numOutputAttributes(); m++) {
            if (inst.valueOutputAttribute(m) == Double.NEGATIVE_INFINITY) {
                labeled = false;
                break;
            }
        }
        return labeled;
    }

    public InstancesHeader getHeader(InstancesHeader originalHeader, FeatureExtractor featureExtractor) {

        ArrayList<Attribute> attributes = featureExtractor.getAttributes(originalHeader, this.bufferSize);

        Instances format = new Instances(getCLICreationString(InstanceStream.class), attributes, 0);
        format.setClassIndex(originalHeader.classIndex());
        return new InstancesHeader(format);
    }

    private Instance extractInstance(double[] originalValues, Buffer partitionBuffer) {

        double[] values = featureExtractor.getResult(originalValues, partitionBuffer);

        for (int i = 0; i < values.length; i++)
            if(Double.isNaN(values[i]))
                values[i] = -Double.MAX_VALUE;

        Instance ret = InstanceUtils.generateInstanceFromValues(values, newHeader);

        return ret;
    }

    private int startAndGetPartition(Instance original) {
        if (newHeader == null) {
            newHeader = this.getHeader(modelContext, this.featureExtractor);
        }
        int partition;
        if (partitionIndex == -1)
            partition = -1;
        else
            partition = (int) original.value(partitionIndex);
        if (!buffer.containsKey(partition)) {
            String bufferType = bufferOpt.getValue();
            boolean isRegressor =  learner instanceof Regressor;
            this.buffer.put(partition, Buffer.getBuffer(bufferType, this.bufferSize, original.numInputAttributes() + 1,
                    this.relevanceRatio, this.classifierRandom, this.timeIndices, this.relevanceModel, this.bufferIndices, isRegressor));
        }
        return partition;
    }





    private void checkIntegrity(Instance expected, Instance actual) {
        assert (expected.classValue() == actual.classValue());
    }

    @Override
    protected Measurement[] getModelMeasurementsImpl() {
        List<Measurement> measurementList = new LinkedList<Measurement>();
        return measurementList.toArray(new Measurement[measurementList.size()]);
    }

    @Override
    public void getModelDescription(StringBuilder out, int indent) {
        ((AbstractClassifier) this.learner).getModelDescription(out, indent);

    }

    @Override
    public void setModelContext(InstancesHeader ih) {
        super.setModelContext(ih);
        learner.setModelContext(this.getHeader(modelContext, this.featureExtractor));
        relevanceModel.setModelContext(ih);
    }

    @Override
    public ImmutableCapabilities defineImmutableCapabilities() {
        if (this.getClass() == BufferLearner.class)
            return new ImmutableCapabilities(Capability.VIEW_STANDARD, Capability.VIEW_LITE);
        else
            return new ImmutableCapabilities(Capability.VIEW_STANDARD);
    }
}
