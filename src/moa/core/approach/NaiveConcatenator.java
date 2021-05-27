package moa.core.approach;

import com.yahoo.labs.samoa.instances.Attribute;
import com.yahoo.labs.samoa.instances.Instances;
import com.yahoo.labs.samoa.instances.InstancesHeader;
import moa.streams.InstanceStream;

import java.util.ArrayList;
import java.util.Arrays;

public class NaiveConcatenator extends Concatenator {
    public NaiveConcatenator() {
    }

    public double[] getResult(double[] event, Buffer buffer) {
        double[][] bufferValues = buffer.getValues(event);
        double[] concatbuffer = EventInstance.concatenate(bufferValues);
        double[][] newEventArray = {event, concatbuffer};
        double[] res = EventInstance.concatenate(newEventArray);
        return res;
    }

    public ArrayList<Attribute> getAttributes(Instances originalHeader, int bufferSize) {

        ArrayList<Attribute> attributes = new ArrayList<>();

        for (int i = 0; i < originalHeader.numAttributes(); i++) {
            Attribute att = originalHeader.attribute(i);
            attributes.add(att);
        }
        for (int i = 0; i < bufferSize; i++) {
            for (int j = 0; j < originalHeader.numAttributes(); j++) {
                Attribute att = originalHeader.attribute(j);
                attributes.add(att);
            }
        }
        return attributes;
    }
}
