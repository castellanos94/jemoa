package com.castellanos94.operators.impl;


public class NSGA3Selection {
 /*   private static Solution idealPoint;
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
    private static final String REFERENCE_POINT_INDEX = "point_reference_index";
    private static final String REFERENCE_POINT_DISTANCE = "point_min_distance";
    private final ReferenceHyperplane Zr;

    private final Data MAX_VALUE, ONE_VALUE, MIN_VALUE, ZER0_VALUE;
    int index, population_size, K;

    public NSGA3Selection(ArrayList<Solution> Rt, int K, ReferenceHyperplane Zr, int population_size, int index)
            throws CloneNotSupportedException {
        this.Rt = Rt;
        this.problem = Rt.get(0).getProblem();
        this.Zr = Zr;
        this.index = index;
        this.K = K;
        this.population_size = population_size;
        this.number_of_objectives = problem.getNumberOfObjectives();
        MAX_VALUE = Data.initByRefType(Rt.get(0).getObjective(0), Double.MAX_VALUE);
        ONE_VALUE = Data.initByRefType(Rt.get(0).getObjective(0), 1.0);
        MIN_VALUE = Data.initByRefType(Rt.get(0).getObjective(0), 0.000001);
        ZER0_VALUE = Data.getZeroByType(Rt.get(0).getObjective(0));
        if (idealPoint == null) {
            idealPoint = new Solution(problem);
            for (int i = 0; i < idealPoint.getObjectives().size(); ++i) {
                idealPoint.setObjective(i, (Data) MAX_VALUE.clone());
            }
        }

        weights = new ArrayList<>();
        for (int i = 0; i < number_of_objectives; ++i) {
            Solution s = new Solution(problem);
            for (int j = 0; j < number_of_objectives; ++j) {
                if (i == j) {
                    s.setObjective(j, (Data) ONE_VALUE.clone());
                } else {
                    s.setObjective(j, (Data) MIN_VALUE.clone());
                }
            }
            weights.add(s);
        }

        if (extremepoints == null) {
            extremepoints = new ArrayList<>();
            for (int i = 0; i < number_of_objectives; i++) {
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
                    A[i][j] = (Data) ZER0_VALUE.clone();
                }
            }
        }

        if (gauss == null) {
            gauss = new Data[problem.getNumberOfObjectives()];

            for (int i = 0; i < gauss.length; ++i) {
                gauss[i] = (Data) ZER0_VALUE.clone();
            }
        }
    }

    public void execute(ArrayList<Solution> Pt) throws CloneNotSupportedException {
        normalize();
        associate();
        niching(Pt);
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
            for (int i = 0; i < number_of_objectives; i++) {
                translate.add(solution.getObjective(i).minus(idealPoint.getObjective(i)));
            }
            solution.getProperties().put(TRANSLATE, translate);
        }
    }

    private Data ASF(Solution x, Solution w) {
        Data max = MIN_VALUE;
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
        Data min_ASF = MAX_VALUE;
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
            boolean iguales = false;
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
            this.gaussianElimination(A, gauss);
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
            gauss[i] = ZER0_VALUE;

        for (int i = N - 1; i >= 0; i -= 1) {
            for (int known = i + 1; known < N; known += 1) {
                A[i][N] = A[i][N].minus(A[i][known].times(gauss[known]));
            }
            gauss[i] = A[i][N].div(A[i][i]);
        }
    }

    private void normalize() throws CloneNotSupportedException {
        Data auxiliar;
        updateIdealPoint();
        translateObjectives();
        foundExtremePoints();
        calculateIntercepts();
        for (Solution solution : Rt) {
            ArrayList<Data> normalizeList = new ArrayList<>();
            for (int i = 0; i < number_of_objectives; i++) {
                Data minus = intercepts.getObjective(i).minus(idealPoint.getObjective(i));
                Data abs_value = minus.abs();
                if (abs_value.compareTo(0.0000000001) > 0) {
                    auxiliar = (intercepts.getObjective(i).minus(idealPoint.getObjective(i)));
                } else {
                    auxiliar =  ZER0_VALUE.plus(0.0000000001);                                 
                }
                Data value = ((ArrayList<Data>) solution.getProperties().get(TRANSLATE)).get(i).div(auxiliar);
                normalizeList.add(value);
            }
            solution.setAttribute(NORMALIZE, normalizeList);
        }
    }
*/
    /**
     * 
     * @param r reference point
     * @param p an individual from Pt
     * @return
     *//*
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
        Solution s, z;
        for (int i = 0; i < Rt.size(); i++) {
            s = Rt.get(i);
            int min_rp = -1;
            Data min_dist = MAX_VALUE;
            for (int j = 0; j < Zr.getNumberOfReferencePoints(); j++) {
                z = Zr.getReferenceSolution(j);
                Data d = perpendicularDistance(z, s);
                if (d.compareTo(min_dist) < 0) {
                    min_dist = d;
                    min_rp = j;
                }
            }
            if (min_rp != -1) {
                s.setAttribute(REFERENCE_POINT_INDEX, min_rp);
                s.setAttribute(REFERENCE_POINT_DISTANCE, min_dist);
            }
            if (s.getRank() < index) {
                Zr.incrementRPMemberSize(min_rp);
            }
        }
    }

    private void niching(ArrayList<Solution> Pt) throws CloneNotSupportedException {
        int k = 1;

        ArrayList<Integer> J = new ArrayList<>();
        ArrayList<Integer> I = new ArrayList<>();
        int pj, pj_value, jsel, imin;
        Data i_dist = MAX_VALUE;
        boolean Zr_disabled[] = new boolean[Zr.getNumberOfReferencePoints()];
        boolean pt_disabled[] = new boolean[Rt.size()];
        while (k <= K) {
            pj = -1;
            for (int l = 0; l < Zr.getNumberOfReferencePoints(); l++) {
                if (!Zr_disabled[l] && pj == -1 || Zr.getRPMemberSize(l) < Zr.getRPMemberSize(pj))
                    pj = l;
            }
            pj_value = Zr.getRPMemberSize(pj);
            J.clear();
            for (int i = 0; i < Zr.getNumberOfReferencePoints(); i++) {
                if (!Zr_disabled[i] && Zr.getRPMemberSize(i) == pj_value)
                    J.add(i);
            }
            if (J.size() > 1)
                jsel = J.get(Tools.getRandom().nextInt(J.size() - 1));
            else if (J.size() == 0)
                jsel = J.get(0);
            else
                jsel = pj;
            I.clear();
            imin = -1;
            for (int i = 0; i < Rt.size(); i++) {
                Solution s = Rt.get(i);
                if (!pt_disabled[i] && s.getRank() == index) {
                    I.add(i);
                    if (imin == -1 || ((Data) s.getAttribute(REFERENCE_POINT_DISTANCE)).compareTo(i_dist) < 0) {
                        imin = i;
                        i_dist = ((Data) s.getAttribute(REFERENCE_POINT_DISTANCE));
                    }
                }
            }
            if (!I.isEmpty()) {
                if (pj_value == 0) {
                    Pt.add( (Solution) Rt.get(imin).clone());

                } else {
                    imin = I.get(Tools.getRandom().nextInt(I.size() - 1));
                    Pt.add( (Solution) Rt.get(imin).clone());
                }
                Zr.incrementRPMemberSize(pj);
                k += 1;
                pt_disabled[imin] = true;
            } else {
                Zr_disabled[jsel] = true;
            }
        }

    }
*/
}