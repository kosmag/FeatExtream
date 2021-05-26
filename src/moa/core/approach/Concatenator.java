package moa.core.approach;

import com.yahoo.labs.samoa.instances.Attribute;
import com.yahoo.labs.samoa.instances.Instances;
import com.yahoo.labs.samoa.instances.InstancesHeader;
import moa.streams.InstanceStream;

import java.util.ArrayList;

abstract public class Concatenator {
    abstract public double[] getResult(double[] event, Buffer buffer);

    abstract public ArrayList<Attribute> getAttributes(Instances originalHeader, int bufferSize);

    static public Concatenator getConcatenator(String name) {
        Concatenator ret = null;
        switch (name) {
            case "naive":
                ret = new NaiveConcatenator();
        }
        return ret;
    }
}
