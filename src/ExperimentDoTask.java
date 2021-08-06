import com.yahoo.labs.samoa.instances.Instance;
import moa.classifiers.meta.AdaptiveRandomForestRegressor;
import moa.core.TimingUtils;

import java.io.IOException;
import java.util.Arrays;


public class ExperimentDoTask {

    public ExperimentDoTask() {
    }

    public static void main(String[] args) throws IOException {
        String[] myArgs = {"EvaluatePrequentialRegression", "-l", "moa.classifiers.functions.MyTargetMean", "-s",
                "(moa.streams.ArffFileStream", "-f", "(C:\\Users\\kosma\\Desktop\\MAGISTER\\Vavel arff\\2018-05-23\\part-0-1.arff)", "-c", "10)"};

        moa.DoTask.main(myArgs);

        String[] myArgs2 = {"EvaluatePrequentialRegression", "-l", "moa.classifiers.functions.MyTargetMean", "-s",
                "moa.streams.BufferStream"};
        moa.DoTask.main(myArgs2);


        String[] myArgs8 = {"EvaluatePrequential", "-l", "(moa.classifiers.BufferLearner", "-r", "0.1", "-c", "cep)", "-s",
                "moa.streams.generators.RandomRBFGenerator"};

        moa.DoTask.main(myArgs8);

        String[] myArgs7 = {"EvaluatePrequentialRegression", "-l", "(moa.classifiers.BufferLearner", "-r", "0.1", "-c", "cep)", "-s",
                "(moa.streams.ArffFileStream", "-f", "(C:\\Users\\kosma\\Desktop\\MAGISTER\\Vavel arff\\2018-05-23\\part-0-1.arff)", "-c", "10)"};

        moa.DoTask.main(myArgs7);

        String[] myArgs6 = {"EvaluatePrequentialRegression", "-l", "(moa.classifiers.BufferLearner", "-c", "cep)", "-s",
                "(moa.streams.ArffFileStream", "-f", "(C:\\Users\\kosma\\Desktop\\MAGISTER\\Vavel arff\\2018-05-23\\part-0-1.arff)", "-c", "10)"};

        moa.DoTask.main(myArgs6);

        String[] myArgs5 = {"EvaluatePrequentialRegression", "-l", "(moa.classifiers.BufferLearner", "-c", "cluster)", "-s",
                "(moa.streams.ArffFileStream", "-f", "(C:\\Users\\kosma\\Desktop\\MAGISTER\\Vavel arff\\2018-05-23\\part-0-1.arff)", "-c", "10)"};

        moa.DoTask.main(myArgs5);

        String[] myArgs4 = {"EvaluatePrequentialRegression", "-l", "(moa.classifiers.BufferLearner", "-c", "featureExtraction)", "-s",
                "(moa.streams.ArffFileStream", "-f", "(C:\\Users\\kosma\\Desktop\\MAGISTER\\Vavel arff\\2018-05-23\\part-0-1.arff)", "-c", "10)"};

        moa.DoTask.main(myArgs4);

        String[] myArgs3 = {"EvaluatePrequentialRegression", "-l", "moa.classifiers.BufferLearner", "-s",
                "(moa.streams.ArffFileStream", "-f", "(C:\\Users\\kosma\\Desktop\\MAGISTER\\Vavel arff\\2018-05-23\\part-0-1.arff)", "-c", "10)"};

        moa.DoTask.main(myArgs3);


    }
}