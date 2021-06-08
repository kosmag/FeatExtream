package moa.core.approach;


import com.yahoo.labs.samoa.instances.DenseInstance;
import com.yahoo.labs.samoa.instances.Instance;
import com.yahoo.labs.samoa.instances.Instances;

import java.util.Arrays;
import java.util.stream.Stream;
import java.util.DoubleSummaryStatistics;

public class InstanceUtils {
    public static double[] getValuesFromInstance(Instance instance) {
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

    public static Instance[] normalize(Instance[] instArray) {
        if (instArray.length == 0)
            return instArray;
        for (int i = 0; i < instArray[0].numAttributes(); i++) {
//            if (instArray[0].classIndex() == i) continue;
            int finalI = i;
            Stream<Double> values = Arrays.stream(instArray).map(inst -> inst.value(finalI));
            double mean = values.mapToDouble(v -> v).average().getAsDouble();
            values = Arrays.stream(instArray).map(inst -> inst.value(finalI));
            double variance = values
                    .map(v -> v - mean)
                    .map(v -> v * v)
                    .mapToDouble(v -> v).average().getAsDouble();
            double eps = 1e-6;
            double stdev = Math.max(Math.sqrt(variance), eps);
            for (Instance inst : instArray) {
                inst.setValue(i, (inst.value(i) - mean) / stdev);
            }
        }
        return instArray;
    }


}
