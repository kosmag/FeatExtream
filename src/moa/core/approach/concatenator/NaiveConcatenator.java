package moa.core.approach.concatenator;

import com.yahoo.labs.samoa.instances.Attribute;
import com.yahoo.labs.samoa.instances.InstancesHeader;
import moa.core.approach.buffer.Buffer;
import moa.core.approach.util.InstanceUtils;

import java.util.ArrayList;

public class NaiveConcatenator extends FeatureExtractor {
    public NaiveConcatenator() {
    }

    public double[] getResult(double[] event, Buffer buffer) {
        double[][] bufferValues = buffer.getValues(event);
        double[] concatbuffer = InstanceUtils.concatenate(bufferValues);
        double[][] newEventArray = {event, concatbuffer};
        double[] res = InstanceUtils.concatenate(newEventArray);
        return res;
    }

    public ArrayList<Attribute> getAttributes(InstancesHeader originalHeader, int bufferSize) {

        ArrayList<Attribute> attributes = new ArrayList<>();

        for (int i = 0; i < originalHeader.numAttributes(); i++) {
            Attribute att = originalHeader.attribute(i);
            attributes.add(att);
        }
        for (int i = 0; i < bufferSize; i++) {
            for (int j = 0; j < originalHeader.numAttributes(); j++) {
                Attribute att = originalHeader.attribute(j);
                attributes.add(new Attribute(att.name() + "_" + i));
            }
        }
        return attributes;
    }

}
