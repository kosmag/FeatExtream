package moa.core.approach.concatenator;

import java.util.Arrays;

public class EventInstance {
    public static double[] concatenate(double[][] buffer) {
        return Arrays.stream(buffer)
                .flatMapToDouble(Arrays::stream)
                .toArray();
    }
}
