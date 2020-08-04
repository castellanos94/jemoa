package com.castellanos94.operators.impl;

import java.util.ArrayList;

import com.castellanos94.datatype.Data;
import com.castellanos94.datatype.Interval;
import com.castellanos94.datatype.RealData;
import com.castellanos94.operators.SelectionOperator;
import com.castellanos94.problems.Problem;
import com.castellanos94.solutions.Solution;

public class NSGA3Selection implements SelectionOperator {
    private static Solution idealPoint;
    private static Solution intercepts;
    private Solution idealSolution;
    private ArrayList<Solution> Rt;
    private ArrayList<Solution> weights;
    private ArrayList<Solution> extremepoints;
    private int number_of_objectives;
    private Data[][] A;
    private Data[] gauss;
    private Problem problem;
    private static final String TRANSLATE = "translate_objectives";
    private static final String NORMALIZE = "normalized_objectis";

    public NSGA3Selection(ArrayList<Solution> Rt) throws CloneNotSupportedException {
        this.Rt = Rt;
        this.problem = Rt.get(0).getProblem();
        boolean first = false;

        Data max = Data.initByRefType(Rt.get(0).getObjective(0), Double.MAX_VALUE);
        Data one = Data.initByRefType(Rt.get(0).getObjective(0), 1.0);
        Data min = Data.initByRefType(Rt.get(0).getObjective(0), 0.000001);
        Data zero = Data.getZeroByType(Rt.get(0).getObjective(0));
        if (idealPoint == null) {
            idealPoint = new Solution(problem);
            first = true;
            for (int i = 0; i < idealPoint.getObjectives().size(); ++i) {
                idealPoint.setObjective(i, (Data) max.clone());
            }
        }

        if (weights == null) {
            Solution s;
            weights = new ArrayList<>();
            for (int i = 0; i < Rt.size(); ++i) {
                s = weights.get(i);
                for (int j = 0; j < number_of_objectives; ++j) {
                    if (i == j) {
                        s.setObjective(j, (Data) one.clone());
                    } else {
                        s.setObjective(j, (Data) min.clone());
                    }
                }
            }
        } // if weights

        if (extremepoints == null) {
            extremepoints = new ArrayList<>();
            for (Solution solution : Rt) {
                extremepoints.add(new Solution(problem));
            }
        }

        if (intercepts == null) {
            intercepts = new Solution(problem);
        }

        if (A == null) {
            A = new Data[problem.getNumberOfObjectives()][problem.getNumberOfObjectives() + 1];

            for (int i = 0; i < A.length; ++i) {
                for (int j = 0; j < A[i].length; ++j) {
                    A[i][j] = (Data) zero.clone();
                }
            }
        }

        if (gauss == null) {
            gauss = new Data[problem.getNumberOfObjectives()];

            for (int i = 0; i < gauss.length; ++i) {
                gauss[i] = (Data) zero.clone();
            }
        }

    }

    @Override
    public void execute(ArrayList<Solution> solutions) {
        // TODO Auto-generated method stub

    }

    private void updateIdealPoint() throws CloneNotSupportedException {
        for (Solution solution : Rt) {
            for (int i = 0; i < number_of_objectives; i++) {
                if (solution.getObjective(i).compareTo(idealPoint.getObjective(i)) < 0)
                    idealPoint.setObjective(i, (Data) solution.getObjective(i).clone());
            }

        }
    }

    private void translateObjectives() {
        for (Solution solution : Rt) {
            ArrayList<Data> translate = new ArrayList<>();
            for (int i = 0; i < number_of_objectives;i++) {
                translate.add(solution.getObjective(i).minus(idealPoint.getObjective(i)));
            }
            solution.getProperties().put(TRANSLATE, translate);
        }
    }

    private Data ASF(Solution x, Solution w) {
        Data max = Data.initByRefType(x.getObjective(0), Double.MIN_VALUE);
        Data p, div;
        ArrayList<Data> translate = (ArrayList<Data>) x.getProperties().get(TRANSLATE);
        for (int i = 0; i < number_of_objectives; i++) {
            p = w.getObjective(i);
            div = translate.get(i).div(p);
            if (div.compareTo(max) > 0)
                max = div;
        }
        return max;
    }

    private void foundExtremePoints() throws CloneNotSupportedException {
        Data min_ASF = new Interval(Double.MIN_VALUE);
        int point = -1;
        Data value;
        for (int i = 0; i < number_of_objectives; i++) {
            for (Solution solution : Rt) {
                value = ASF(solution, weights.get(i));
                if (value.compareTo(min_ASF) < 0) {
                    point = i;
                    min_ASF = value;
                }
            }
            extremepoints.set(i, (Solution) Rt.get(point).clone());

        }
    }

    private void calculateIntercepts() {
        boolean duplicado = false;
        for (int i = 0; !duplicado && i < extremepoints.size(); i++) {
            boolean iguales = true;
            ArrayList<Data> extremeI = (ArrayList<Data>) extremepoints.get(i).getProperties().get(TRANSLATE);
            for (int j = i + 1; iguales && j < extremepoints.size(); j++) {

                ArrayList<Data> extremeJ = (ArrayList<Data>) extremepoints.get(j).getProperties().get(TRANSLATE);
                for (int k = 0; iguales && k < number_of_objectives; k++) {
                    iguales = extremeI.get(k) == extremeJ.get(k);
                }
            }
            duplicado = iguales;
        }
        if (duplicado) {
            for (int i = 0; i < extremepoints.size(); i++) {
                intercepts.setObjective(i,
                        ((ArrayList<Data>) extremepoints.get(i).getProperties().get(TRANSLATE)).get(i));
            }
        } else {
            for (int i = 0; i < extremepoints.size(); i++) {
                for (int j = 0; j < number_of_objectives; j++) {
                    A[i][j] = ((ArrayList<Data>) extremepoints.get(i).getProperties().get(TRANSLATE)).get(j);
                }
                A[i][extremepoints.size()] = Data.getOneByType(Rt.get(0).getObjective(0));

            }
            for (int i = 0; i < extremepoints.size(); i++) {
                RealData uno = RealData.ONE;
                intercepts.setObjective(i, uno.div(gauss[i]));
            }
        }
    }

    private void gaussianElimination(Data[][] A, Data[] gauss) {
        int N = A.length;

        for (int base = 0; base < N - 1; base += 1) {
            for (int target = base + 1; target < N; target += 1) {
                Data ratio = A[target][base].div(A[base][base]);
                for (int term = 0; term < A[base].length; term += 1) {
                    A[target][term] = A[target][term].minus(A[base][term].times(ratio));
                }
            }
        }

        for (int i = 0; i < N; i++)
            gauss[i] = new Interval(0.0);

        for (int i = N - 1; i >= 0; i -= 1) {
            for (int known = i + 1; known < N; known += 1) {
                A[i][N] = A[i][N].minus(A[i][known].times(gauss[known]));
            }
            gauss[i] = A[i][N].div(A[i][i]);
        }
    }

    @Override
    public ArrayList<Solution> getParents() {
        // TODO Auto-generated method stub
        return null;
    }

    private void normalize() throws CloneNotSupportedException {
        Data auxiliar;
        updateIdealPoint();
        translateObjectives();
        foundExtremePoints();
        calculateIntercepts();
        for (Solution solution : Rt) {
            for (int i = 0; i < number_of_objectives; i++) {
                Data minus = intercepts.getObjective(i).minus(idealPoint.getObjective(i));
                Data abs_value = minus.abs();
                if (abs_value.compareTo(0.0000000001) > 0) {
                    auxiliar = (intercepts.getObjective(i).minus(idealPoint.getObjective(i)));
                } else {
                    auxiliar = new Interval(0.0000000001);
                }
                Data value = ((ArrayList<Data>) solution.getProperties().get(TRANSLATE)).get(i);
                ((ArrayList<Data>) solution.getAttribute(NORMALIZE)).set(i, value);
            }
        }
    }

    /**
     * 
     * @param r reference point
     * @param p an individual from Pt
     * @return
     */
    public Data perpendicularDistance(Solution r, Solution p) {
        Data numerator = Data.getZeroByType(r.getObjective(0));
        Data denominator = Data.getZeroByType(r.getObjective(0));
        ArrayList<Data> normalizeData = (ArrayList<Data>) p.getAttribute(NORMALIZE);
        for (int i = 0; i < r.getObjectives().size(); i += 1) {
            numerator = numerator.plus(r.getObjective(i).times(normalizeData.get(i)));
            denominator = denominator.plus(r.getObjective(i).times(r.getObjective(i)));
        }

        Data k = numerator.div(denominator);

        Data d = Data.getZeroByType(r.getObjective(0));

        for (int i = 0; i < r.getObjectives().size(); i += 1) {
            Data res = k.times(r.getObjective(i)).minus(normalizeData.get(i));
            d = d.plus(res.times(res));
        }

        return d.sqrt();
    }

    private void associate() {

    }

    private void niching(ArrayList<Solution> Pt, int indexFront) {

    }

}