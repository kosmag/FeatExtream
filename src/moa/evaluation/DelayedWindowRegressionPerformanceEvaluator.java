/*
 *    WindowRegressionPerformanceEvaluator.java
 *    Copyright (C) 2011 University of Waikato, Hamilton, New Zealand
 *    @author Albert Bifet (abifet at cs dot waikato dot ac dot nz)
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

import com.github.javacliparser.IntOption;
import com.yahoo.labs.samoa.instances.Instance;
import com.yahoo.labs.samoa.instances.Prediction;
import moa.core.Example;
import moa.core.Measurement;
import moa.core.ObjectRepository;
import moa.core.approach.ArrivedEvent;
import moa.options.AbstractOptionHandler;
import moa.tasks.TaskMonitor;

import java.util.HashMap;
import java.util.Map;

/**
 * Regression evaluator that updates evaluation results using a sliding window.
 *
 * @author Albert Bifet (abifet at cs dot waikato dot ac dot nz)
 * @version $Revision: 7 $
 */
public class DelayedWindowRegressionPerformanceEvaluator extends AbstractOptionHandler
        implements RegressionPerformanceEvaluator {

    private static final long serialVersionUID = 1L;

    public IntOption widthOption = new IntOption("width",
            'w', "Size of Window", 1000);
    public IntOption idIndexOpt = new IntOption("idIndex", 'i',
            "Id index", 0, 0, Integer.MAX_VALUE);
    protected double TotalweightObserved = 0;

    protected Estimator weightObserved;
    protected Estimator firstWeightObserved;

    protected Estimator firstSquareError;

    protected Estimator firstAverageError;
    protected Estimator lastSquareError;

    protected Estimator lastAverageError;

    protected int numClasses;
    private int idIndex;

    Map<Integer, double[]> arrivedEvents;

    public class Estimator {

        protected double[] window;

        protected int posWindow;

        protected int lenWindow;

        protected int SizeWindow;

        protected double sum;

        public Estimator(int sizeWindow) {
            window = new double[sizeWindow];
            SizeWindow = sizeWindow;
            posWindow = 0;
        }

        public void add(double value) {
            sum -= window[posWindow];
            sum += value;
            window[posWindow] = value;
            posWindow++;
            if (posWindow == SizeWindow) {
                posWindow = 0;
            }
        }

        public double total() {
            return sum;
        }
    }

    /*   public void setWindowWidth(int w) {
    this.width = w;
    reset();
    }*/
    @Override
    public void reset() {
        reset(this.numClasses);
    }

    public void reset(int numClasses) {
        this.numClasses = numClasses;
        this.weightObserved = new Estimator(this.widthOption.getValue());
        this.firstWeightObserved = new Estimator(this.widthOption.getValue());
        this.firstSquareError = new Estimator(this.widthOption.getValue());
        this.firstAverageError = new Estimator(this.widthOption.getValue());
        this.lastSquareError = new Estimator(this.widthOption.getValue());
        this.lastAverageError = new Estimator(this.widthOption.getValue());
        this.idIndex = this.idIndexOpt.getValue();
        this.arrivedEvents = new HashMap<>();

        this.TotalweightObserved = 0;
    }

    @Override
    public void addResult(Example<Instance> example, double[] prediction) {
        Instance inst = example.getData();
        double weight = inst.weight();
        if (weight > 0.0) {
            if (TotalweightObserved == 0) {
                reset(inst.dataset().numClasses());
            }
        }
        int eventId = getId(inst);
        boolean labeled = isLabeled(inst);


        if (!labeled) {
            if (!arrivedEvents.containsKey(eventId)) {
                arrivedEvents.put(eventId, prediction);
            }
        } else {
            double[] firstPred = null;
            if (arrivedEvents.containsKey(eventId)) {
                firstPred = arrivedEvents.get(eventId);
                arrivedEvents.remove(eventId);
            }
            if (weight > 0.0) {
                this.TotalweightObserved += weight;
                this.weightObserved.add(weight);
                if(firstPred != null && firstPred.length > 0) {
                    this.firstWeightObserved.add(weight);

                    this.firstSquareError.add((inst.classValue() - firstPred[0]) * (inst.classValue() - firstPred[0]));
                    this.firstAverageError.add(Math.abs(inst.classValue() - firstPred[0]));
                }
                if (prediction.length > 0) {
                    this.lastSquareError.add((inst.classValue() - prediction[0]) * (inst.classValue() - prediction[0]));
                    this.lastAverageError.add(Math.abs(inst.classValue() - prediction[0]));
                }
                //System.out.println(inst.classValue()+", "+prediction[0]);
            }
        }

    }

    private int getId(Instance original) {
        int id = (int) original.value(idIndex);
        return id;
    }
    private boolean isLabeled(Instance inst) {
        boolean labeled = true;
        for (int m = 0; m < inst.numOutputAttributes(); m++) {
            if (Double.isNaN(inst.valueOutputAttribute(m))) {
                labeled = false;
                break;
            }
        }
        return labeled;
    }


    @Override
    public Measurement[] getPerformanceMeasurements() {
        return new Measurement[]{
                    new Measurement("classified instances",
                    getTotalWeightObserved()),
                    new Measurement("classified first instances",
                        getFirstTotalWeightObserved()),
                    new Measurement("mean absolute error - first measurement",
                    getFirstMeanError()),
                    new Measurement("root mean squared error - first measurement",
                    getFirstSquareError()),
                    new Measurement("mean absolute error - last measurement",
                        getLastMeanError()),
                    new Measurement("root mean squared error - last measurement",
                        getLastSquareError())};
    }

    public double getTotalWeightObserved() {
        return this.weightObserved.total();
    }
    public double getFirstTotalWeightObserved() {
        return this.firstWeightObserved.total();
    }

    public double getFirstMeanError() {
        return this.firstWeightObserved.total() > 0.0 ? this.firstAverageError.total()
                / this.firstWeightObserved.total() : 0.0;
    }
    public double getLastMeanError() {
        return this.weightObserved.total() > 0.0 ? this.lastAverageError.total()
                / this.weightObserved.total() : 0.0;
    }

    public double getFirstSquareError() {
        return Math.sqrt(this.firstWeightObserved.total() > 0.0 ? this.firstSquareError.total()
                / this.firstWeightObserved.total() : 0.0);
    }
    public double getLastSquareError() {
        return Math.sqrt(this.weightObserved.total() > 0.0 ? this.lastSquareError.total()
                / this.weightObserved.total() : 0.0);
    }
    @Override
    public void getDescription(StringBuilder sb, int indent) {
        Measurement.getMeasurementsDescription(getPerformanceMeasurements(),
                sb, indent);
    }

    @Override
    public void prepareForUseImpl(TaskMonitor monitor,
            ObjectRepository repository) {
    }
    

	@Override
	public void addResult(Example<Instance> testInst, Prediction prediction) {
		double votes[];
		if(prediction==null)
			votes = new double[0];
		else
			votes=prediction.getVotes();
		addResult(testInst, votes);
		
	}
}
