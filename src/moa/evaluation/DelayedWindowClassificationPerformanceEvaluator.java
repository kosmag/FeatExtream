/*
 *    WindowClassificationPerformanceEvaluator.java
 *    Copyright (C) 2009 University of Waikato, Hamilton, New Zealand
 *    @author Albert Bifet (abifet@cs.waikato.ac.nz)
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
package moa.evaluation;

import com.yahoo.labs.samoa.instances.Instance;
import moa.capabilities.Capability;
import moa.capabilities.ImmutableCapabilities;

import com.github.javacliparser.IntOption;
import moa.core.Example;
import moa.core.Measurement;
import moa.core.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Classification evaluator that updates evaluation results using a sliding
 * window.
 *
 * @author Albert Bifet (abifet at cs dot waikato dot ac dot nz)
 * @author Jean Paul Barddal (jpbarddal@gmail.com)
 * @version $Revision: 8 $
 *
 *
 */
public class DelayedWindowClassificationPerformanceEvaluator extends BasicClassificationPerformanceEvaluator {

    private static final long serialVersionUID = 1L;

    public IntOption widthOption = new IntOption("width",
            'w', "Size of Window", 1000);
    public IntOption idIndexOpt = new IntOption("idIndex", 'i',
            "Id index", 0, 0, Integer.MAX_VALUE);

    protected Estimator weightCorrectFirst;
    protected Estimator weightCorrectLast;

    private int idIndex;
    private double myTotalWeightObserved;
    private boolean start = true;
    Map<Integer, Integer> arrivedEvents;
    @Override
    public void reset() {
        super.reset();
        this.weightCorrectFirst = newEstimator();
        this.weightCorrectLast = newEstimator();
        this.idIndex = this.idIndexOpt.getValue();
        this.arrivedEvents = new HashMap<>();
        this.myTotalWeightObserved = 0;

    }
    @Override
    protected Estimator newEstimator() {
        return new WindowEstimator(this.widthOption.getValue());
    }

    @Override
    public ImmutableCapabilities defineImmutableCapabilities() {
        if (this.getClass() == DelayedWindowClassificationPerformanceEvaluator.class)
            return new ImmutableCapabilities(Capability.VIEW_STANDARD, Capability.VIEW_LITE);
        else
            return new ImmutableCapabilities(Capability.VIEW_STANDARD);
    }


    @Override
    public void addResult(Example<Instance> example, double[] classVotes) {
        if (this.start) {
            reset();
            this.start = false;
        }
        Instance inst = example.getData();
        double weight = inst.weight();
        int eventId = getId(inst);
        if(inst.classIsMissing()){
            if (!arrivedEvents.containsKey(eventId)) {
                arrivedEvents.put(eventId, Utils.maxIndex(classVotes));
            }
        } else {
            Integer firstPred = null;
            if (arrivedEvents.containsKey(eventId)) {
                firstPred = arrivedEvents.get(eventId);
                arrivedEvents.remove(eventId);
            }
            int trueClass = (int) inst.classValue();
            int predictedClass = Utils.maxIndex(classVotes);
            if (weight > 0.0) {
                this.myTotalWeightObserved += weight;
                this.weightCorrectLast.add(predictedClass == trueClass ? weight : 0);
            }
            if(firstPred != null) {
                this.weightCorrectFirst.add(firstPred == trueClass ? weight : 0);
            }
        }
    }

    private int getId(Instance original) {
        int id = (int) original.value(idIndex);
        return id;
    }

    @Override
    public Measurement[] getPerformanceMeasurements() {
        ArrayList<Measurement> measurements = new ArrayList<Measurement>();
        measurements.add(new Measurement("classified instances", this.myTotalWeightObserved));
        measurements.add(new Measurement("classifications correct (percent)- first measurement", getFirst() * 100.0));
        measurements.add(new Measurement("classifications correct (percent)- last measurement", getLast() * 100.0));

        Measurement[] result = new Measurement[measurements.size()];

        return measurements.toArray(result);

    }

    private double getLast() {
        return this.weightCorrectLast.estimation();
    }

    private double getFirst() {
        return this.weightCorrectFirst.estimation();
    }


    public class WindowEstimator implements Estimator {

        protected double[] window;

        protected int posWindow;

        protected int lenWindow;

        protected int SizeWindow;

        protected double sum;

        protected double qtyNaNs;

        public WindowEstimator(int sizeWindow) {
            window = new double[sizeWindow];
            SizeWindow = sizeWindow;
            posWindow = 0;
            lenWindow = 0;
        }

        public void add(double value) {
            double forget = window[posWindow];
            if(!Double.isNaN(forget)){
                sum -= forget;
            }else qtyNaNs--;
            if(!Double.isNaN(value)) {
                sum += value;
            }else qtyNaNs++;
            window[posWindow] = value;
            posWindow++;
            if (posWindow == SizeWindow) {
                posWindow = 0;
            }
            if (lenWindow < SizeWindow) {
                lenWindow++;
            }
        }

        public double estimation(){
            return sum / (lenWindow - qtyNaNs);
        }

    }

}
