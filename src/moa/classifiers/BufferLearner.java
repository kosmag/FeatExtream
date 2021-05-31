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
import moa.core.approach.Buffer;
import moa.core.approach.Concatenator;
import moa.core.approach.InstanceUtils;
import moa.options.ClassOption;
import moa.streams.InstanceStream;
import moa.tasks.TaskMonitor;

import java.util.*;

public class BufferLearner extends AbstractClassifier implements MultiClassClassifier,
        CapabilitiesHandler, Regressor {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Override
    public String getPurposeString() {
        return "Buffer learner";
    }

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

    public IntOption idIndexOpt = new IntOption("idIndex", 'i',
            "Id index", 0, 0, Integer.MAX_VALUE);

    public IntOption binNoOpt = new IntOption("binNo", 'z',
            "Bin number", 5, 1, Integer.MAX_VALUE);

    public IntOption reevalFrequencyOpt = new IntOption("reevalFrequency", 'f',
            "Reeavluation frequency", 10, 1, Integer.MAX_VALUE);

    private int bufferSize;
    private int partitionIndex;
    private int[] timeIndices;
    private int idIndex;
    private int binNo;
    private int reevalFrequency;
    private double relevanceRatio;
    private Concatenator concatenator;
    private Map<Integer, Buffer> buffer;
    Map<Integer, ArrivedEvent> arrivedEvents;

    protected InstancesHeader newHeader;

    public Classifier learner;
    public Classifier relevanceModel;

    public BufferLearner() {
    }

    public BufferLearner(String learnerCLI, int bufferSize, double relevanceRatio, String relevanceModelCLI, int randomSeed,
                         String concatenator, String buffer, int partitionIndex, String timeIndices, int idIndex,
                         int binNo, int reevalFrequency) {
        this.learnerOpt.setValueViaCLIString(learnerCLI);
        this.bufferSizeOpt.setValue(bufferSize);
        this.randomSeedOption.setValue(randomSeed);
        this.relevanceRatioOpt.setValue(relevanceRatio);
        this.relevanceModelOpt.setValueViaCLIString(relevanceModelCLI);
        this.concatenatorOpt.setValue(concatenator);
        this.bufferOpt.setValue(buffer);
        this.partitionIndexOpt.setValue(partitionIndex);
        this.timeIndicesOpt.setValue(timeIndices);
        this.idIndexOpt.setValue(idIndex);
        this.binNoOpt.setValue(binNo);
        this.reevalFrequencyOpt.setValue(reevalFrequency);
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
        this.concatenator = Concatenator.getConcatenator(concatenatorOpt.getValue());
        this.buffer = new HashMap<>();
        this.partitionIndex = partitionIndexOpt.getValue();
        this.timeIndices = getTimeIndices(timeIndicesOpt.getValue());
        this.arrivedEvents = new HashMap<>();
        this.idIndex = idIndexOpt.getValue();
        this.binNo = binNoOpt.getValue();
        this.reevalFrequency = reevalFrequencyOpt.getValue();
    }

    private int[] getTimeIndices(String value) {
        if (value == "") return new int[0];
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
        int eventId = getId(inst);

        if (!labeled) {
            if (!arrivedEvents.containsKey(eventId)) {
                arrivedEvents.put(eventId, new ArrivedEvent(inst, reevalFrequency, binNo));
                ArrivedEvent event = arrivedEvents.get(eventId);
                event.addPrediction(getResult(event.instance), 0);
            }
        } else {
            if (arrivedEvents.containsKey(eventId)) {
                ArrivedEvent event = arrivedEvents.get(eventId);
                event.addPrediction(getResult(event.instance), binNo + 1);
                updatePerformanceMeasures();
                arrivedEvents.remove(eventId);

            }
            for (int id : arrivedEvents.keySet()) {
                ArrivedEvent event = arrivedEvents.get(id);

                event.addPrediction(getResult(event.instance), -1);
            }
        }

        updateBufferAndTrain(inst, labeled, updatable);


    }

    private double getResult(Instance instance) {
        if (learner instanceof Regressor)
            return Arrays.stream(learner.getVotesForInstance(instance)).average().getAsDouble();
        else
            return Utils.maxIndex(getVotesForInstance(instance));
    }

    private void updatePerformanceMeasures() {
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
            this.concatenator.train(inst);
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

    public InstancesHeader getHeader(InstancesHeader originalHeader, Concatenator concatenator) {

        ArrayList<Attribute> attributes = concatenator.getAttributes(originalHeader, this.bufferSize);

        Instances format = new Instances(getCLICreationString(InstanceStream.class), attributes, 0);
        format.setClassIndex(originalHeader.classIndex());
        return new InstancesHeader(format);
    }

    private Instance extractInstance(double[] originalValues, Buffer partitionBuffer) {

        double[] values = concatenator.getResult(originalValues, partitionBuffer);

        Instance ret = InstanceUtils.generateInstanceFromValues(values, newHeader);

        return ret;
    }

    private int startAndGetPartition(Instance original) {
        if (newHeader == null) {
            newHeader = this.getHeader(modelContext, this.concatenator);
        }
        int partition;
        if (partitionIndex == -1)
            partition = -1;
        else
            partition = (int) original.value(partitionIndex);
        if (!buffer.containsKey(partition)) {
            String bufferType = bufferOpt.getValue();
            if(!(learner instanceof Regressor));
            bufferType += "_classifier";
            this.buffer.put(partition, Buffer.getBuffer(bufferType, this.bufferSize, original.numInputAttributes() + 1,
                    this.relevanceRatio, this.classifierRandom, this.timeIndices, this.relevanceModel));
        }
        return partition;
    }


    private int getId(Instance original) {
        int id = (int) original.value(idIndex);
        return id;
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
        learner.setModelContext(ih);
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