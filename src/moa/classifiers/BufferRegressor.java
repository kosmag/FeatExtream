package moa.classifiers;

import com.github.javacliparser.FloatOption;
import com.github.javacliparser.IntOption;
import com.github.javacliparser.StringOption;
import com.yahoo.labs.samoa.instances.*;
import moa.core.Measurement;
import moa.core.ObjectRepository;
import moa.core.approach.Buffer;
import moa.core.approach.Concatenator;
import moa.options.ClassOption;
import moa.streams.InstanceStream;
import moa.tasks.TaskMonitor;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class BufferRegressor extends AbstractClassifier implements Regressor {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Override
    public String getPurposeString() {
        return "Random Active learning classifier for evolving data streams";
    }

    public ClassOption learnerOpt = new ClassOption("learner", 'l',
            "Learner to train.", Classifier.class, "moa.classifiers.meta.AdaptiveRandomForestRegressor");
    public IntOption bufferSizeOpt = new IntOption("bufferSize", 'n',
            "Buffer size", 10, 1, 1000);

    public FloatOption relevanceRatioOpt = new FloatOption("relevanceRatio", 'r',
            "Relevance ratio", 1, 0.001, 1);

    public StringOption concatenatorOpt = new StringOption("concatenator", 'c',
            "Concatenator", "naive");

    public StringOption bufferOpt = new StringOption("buffer", 'b',
            "Buffer", "random");

    private int bufferSize;
    private double relevanceRatio;
    private Concatenator concatenator;
    private Buffer buffer;

    protected Instances newHeader;

    public Classifier learner;

    public BufferRegressor() {
    }

    public BufferRegressor(String learnerCLI, int bufferSize, double relevanceRatio, int randomSeed, String concatenator, String buffer) {
        this.learnerOpt.setValueViaCLIString(learnerCLI);
        this.bufferSizeOpt.setValue(bufferSize);
        this.randomSeedOption.setValue(randomSeed);
        this.relevanceRatioOpt.setValue(relevanceRatio);
        this.concatenatorOpt.setValue(concatenator);
        this.bufferOpt.setValue(buffer);
    }

    @Override
    public void prepareForUseImpl(TaskMonitor monitor, ObjectRepository repository) {
        this.randomSeed = this.randomSeedOption.getValue();
        this.classifierRandom = new Random(this.randomSeed);

        create();
        learner.prepareForUse(monitor, repository);
    }

    private void create() {
        this.learner = (Classifier) getPreparedClassOption(learnerOpt);
        this.bufferSize = bufferSizeOpt.getValue();
        this.relevanceRatio = relevanceRatioOpt.getValue();
        this.concatenator = Concatenator.getConcatenator(concatenatorOpt.getValue());
    }

    @Override
    public boolean isRandomizable() {
        return true;
    }

    @Override
    public double[] getVotesForInstance(Instance inst) {
        Instance extractedInstance = extractInstance(inst);
        return this.learner.getVotesForInstance(extractedInstance);
    }

    @Override
    public void resetLearningImpl() {
        this.learner = ((Classifier) getPreparedClassOption(this.learnerOpt)).copy();
        this.learner.resetLearning();
    }

    @Override
    public void trainOnInstanceImpl(Instance inst) {


        Instance extractedInstance = extractInstance(inst);

        this.learner.trainOnInstance(extractedInstance);

    }

    public Instances getHeader(Instances originalHeader, Concatenator concatenator) {

        ArrayList<Attribute> attributes = concatenator.getAttributes(originalHeader, this.bufferSize);

        Instances format = new Instances(getCLICreationString(InstanceStream.class), attributes, 0);
        format.setClassIndex(originalHeader.numAttributes() - 1);
        return new InstancesHeader(format);
    }

    private Instance extractInstance(Instance original) {
        if (buffer == null) {
            this.newHeader = this.getHeader(original.dataset(), this.concatenator);
            this.buffer = Buffer.getBuffer(bufferOpt.getValue(), this.bufferSize, original.numInputAttributes(), this.relevanceRatio, this.classifierRandom);
        }

        double[] originalValues = getValuesFromInstance(original);
        double[] values = concatenator.getResult(originalValues, buffer);

        Instance ret = generateInstanceFromValues(values);
        try {
            checkIntegrity(original, ret);
        } catch (Exception e) {
            e.printStackTrace();
        }
        buffer.nextElement(originalValues);

        return ret;
    }

    private double[] getValuesFromInstance(Instance instance) {
        double[] ret = new double[instance.numAttributes()];

        int ix = 0;
        for (int i = 0; i < instance.dataset().numAttributes(); i++) {
            if (instance.dataset().classIndex() != i) {
                ret[ix] = instance.value(i);
                ix++;
            }
        }

        if (instance.classIndex() != -1) {
            ret[ret.length - 1] = instance.classValue();
        }
        return ret;
    }

    private Instance generateInstanceFromValues(double[] values) {
        Instance instnc = new DenseInstance(1.0, values);
        instnc.setDataset(newHeader);
        return instnc;
    }


    private void checkIntegrity(Instance expected, Instance actual) {
        assert  (expected.classValue() == actual.classValue());
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
    }
}
