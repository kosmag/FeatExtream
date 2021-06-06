package moa.core.approach;


import com.yahoo.labs.samoa.instances.DenseInstance;
import com.yahoo.labs.samoa.instances.Instance;
import com.yahoo.labs.samoa.instances.Instances;

public class InstanceUtils {
    public static double[] getValuesFromInstance(Instance instance){
        double[] ret = new double[instance.numAttributes()];

        int ix = 0;
        for (int i = 0; i < instance.numAttributes(); i++) {

            ret[ix] = instance.value(i);
            ix++;

        }
        return ret;
    }


    public static Instance generateInstanceFromValues(double[] values, Instances header) {
        Instance instnc = new DenseInstance(1.0, values);
        instnc.setDataset(header);
        return instnc;
    }

}
