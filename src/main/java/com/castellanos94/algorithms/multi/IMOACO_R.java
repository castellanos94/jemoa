package com.castellanos94.algorithms.multi;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.castellanos94.algorithms.AbstractAlgorithm;
import com.castellanos94.datatype.Data;
import com.castellanos94.datatype.RealData;
import com.castellanos94.problems.Problem;
import com.castellanos94.solutions.Solution;
import com.castellanos94.utils.Distance;
import com.castellanos94.utils.HeapSort;
import com.castellanos94.utils.ReferenceHyperplane;
import com.castellanos94.utils.Tools;
import com.google.common.math.BigIntegerMath;

/**
 * Falcón-Cardona, J.G., Coello Coello, C.A. A new indicator-based
 * many-objective ant colony optimizer for continuous search spaces. Swarm
 * Intell 11, 71–100 (2017). https://doi.org/10.1007/s11721-017-0133-x
 */
public class IMOACO_R<S extends Solution<?>> extends AbstractAlgorithm<S> {
    public static String R2_Alpha_KEY = "R2-rank-alpha";
    public static String NORMALIZE_KEY = "imoaco-norm";
    public static String BEST_UTILITY_KEY = "u*";
    protected ArrayList<Data> idealPoint;
    protected ArrayList<Data> nadirPoint;
    /*
     * protected ArrayList<Data> zMin; protected ArrayList<Data> zMax;
     */
    protected ArrayList<ArrayList<Data>> record;
    protected final double q;
    protected final double xi;
    protected final int h;
    protected final int maxIterations;
    protected final int N;

    /**
     * 
     * @param problem       continuos problem
     * @param maxIterations G_max
     * @param q             diversification process control parameter
     * @param xi            convergence rate control parameter
     * @param h             proportional parameter, using for the construction of
     *                      the simplex-lattice on the SLD in order to create set of
     *                      N convex weight vectors. N is equally used as the number
     *                      of ants
     */
    public IMOACO_R(Problem<S> problem, int maxIterations, double q, double xi, int h) {
        super(problem);
        this.maxIterations = maxIterations;
        this.q = q;
        this.xi = xi;
        this.h = h;
        // {q=objectives, m=h} = (q+m-1)!/(m!(q-1)!)
        this.N = BigIntegerMath.factorial(problem.getNumberOfObjectives() + h - 1).divide(
                BigIntegerMath.factorial(h).multiply(BigIntegerMath.factorial(problem.getNumberOfObjectives() - 1)))
                .intValueExact();
        /*
         * this.zMax = new ArrayList<>(problem.getNumberOfObjectives()); this.zMin = new
         * ArrayList<>(problem.getNumberOfObjectives()); this.nadirPoint = new
         * ArrayList<>(problem.getNumberOfObjectives()); this.idealPoint = new
         * ArrayList<>(problem.getNumberOfObjectives()); for (int index = 0; index <
         * problem.getNumberOfObjectives(); index++) { zMax.set(index, null);
         * zMin.set(index, null); nadirPoint.set(index, null); idealPoint.set(index,
         * null); }
         */
    }

    @Override
    public void execute() {
        this.init_time = System.currentTimeMillis();
        ArrayList<ArrayList<Data>> LAMBDA = generateWeight();
        this.solutions = new ArrayList<>(this.N);
        // pheromenes T = solutions
        for (int index = 0; index < this.N; index++) {
            S randomSolution = problem.randomSolution();
            problem.evaluate(randomSolution);
            problem.evaluateConstraint(randomSolution);
            this.solutions.add(randomSolution);
        }
        this.idealPoint = new ArrayList<>(solutions.get(0).getObjectives());
        this.nadirPoint = new ArrayList<>(solutions.get(0).getObjectives());
        for (S p : solutions) {
            for (int index = 0; index < problem.getNumberOfObjectives(); index++) {
                if (idealPoint.get(index).compareTo(p.getObjective(index)) > 0) {
                    idealPoint.set(index, p.getObjective(index).copy());
                }
                if (nadirPoint.get(index).compareTo(p.getObjective(index)) < 0) {
                    nadirPoint.set(index, p.getObjective(index).copy());
                }
            }
        }
        normalize(this.solutions, idealPoint, nadirPoint);
        this.record = new ArrayList<>();
        this.record.add(nadirPoint);
        R2Ranking(solutions, idealPoint, LAMBDA);
        this.solutions.sort((a, b) -> Integer.compare(a.getRank(), b.getRank()));
        for (int iteration = 0; iteration < maxIterations; iteration++) {
            for (int ant = 0; ant < this.N; ant++) {
                ArrayList<S> ns = searchEngine(solutions);
            }
        }
        this.computeTime = System.currentTimeMillis() - this.init_time;

    }

    private ArrayList<S> searchEngine(ArrayList<S> solutions) {
        ArrayList<S> rs = new ArrayList<>();
        double[] weight = new double[this.N];
        double totalWeight = 0.0;
        for (int index = 0; index < this.N; index++) {
            S s_k = solutions.get(index);
            // w_j
            weight[index] = (Math.exp(-Math.pow(s_k.getRank() - 1, 2) / (2 * this.q * this.q * this.N * this.N)))
                    / (this.q * this.N * Math.sqrt(2 * Math.PI));
            totalWeight += weight[index];
        }
        // Ants
        for (int antIndex = 0; antIndex < this.N; antIndex++) {
            //Explora el espacio de solucion k para seleccionar una variable 
            S tmp = problem.getEmptySolution();
            for (int k = 0; k < this.problem.getNumberOfDecisionVars(); k++) {
                if(Tools.getRandom().nextDouble() < weight[k]/totalWeight){

                }
            }
        }
        return rs;
    }

    private void normalize(ArrayList<S> solutions, ArrayList<Data> zmin, ArrayList<Data> zmax) {
        for (S solution : solutions) {
            ArrayList<Data> fnorm = new ArrayList<>();
            for (int index = 0; index < problem.getNumberOfObjectives(); index++) {
                fnorm.add(solution.getObjective(index).minus(zmin.get(index))
                        .div(zmax.get(index).minus(zmin.get(index))));
            }
            solution.setAttribute(NORMALIZE_KEY, fnorm);
        }
    }

    protected ArrayList<ArrayList<Data>> generateWeight() {
        ReferenceHyperplane<S> referenceHyperplane = new ReferenceHyperplane<>(problem.getNumberOfObjectives(), h);
        referenceHyperplane.execute();
        return referenceHyperplane.transformToData();
    }

    /**
     * R2-ranking (Hernandez Gomez and Coello Colleo 2015)
     * 
     * @param P          population P
     * @param idealPoint z^min
     * @param LAMBDA     set of weight vectors Λ
     */
    protected void R2Ranking(ArrayList<S> P, ArrayList<Data> idealPoint, ArrayList<ArrayList<Data>> LAMBDA) {

        for (S p : P) {
            p.setRank(Integer.MAX_VALUE);
        }
        for (int i = 0; i < LAMBDA.size(); i++) {
            List<Data> lambda = LAMBDA.get(i);
            for (S p : P) {
                p.setAttribute(R2_Alpha_KEY, ASF(getFNorm(p), idealPoint, lambda));
            }
            Comparator<S> comparator = (a, b) -> {
                Data alpha_a = (Data) a.getAttribute(R2_Alpha_KEY);
                Data alpha_b = (Data) b.getAttribute(R2_Alpha_KEY);
                return alpha_a.compareTo(alpha_b);
            };
            comparator = comparator.thenComparing((a, b) -> {
                Data d1 = Distance.chebyshevDistance(getFNorm(a), lambda);
                Data d2 = Distance.chebyshevDistance(getFNorm(b), lambda);
                return d1.compareTo(d2);
            });
            HeapSort<S> sorter = new HeapSort<>(comparator);
            sorter.sort(P);
            int rank = 0;
            for (int index = 0; index < P.size(); index++) {
                S p = P.get(index);
                if (rank < p.getRank()) {
                    p.setRank(rank);
                    p.setAttribute(BEST_UTILITY_KEY, p.getAttribute(R2_Alpha_KEY));
                }
                rank++;
            }

        }
    }

    /**
     * Eq. 9: u_asf(v|r, λ) = max_{i \in {1... m }} { | v_i - r_i|/lambda_i}
     * 
     * @param objectives v
     * @param idealPoint r
     * @param lambda     λ
     * @return
     */
    protected Data ASF(List<Data> objectives, List<Data> idealPoint, List<Data> lambda) {
        Data result = Data.getOneByType(objectives.get(0));
        for (int i = 0; i < objectives.size(); i++) {
            if (lambda.get(i).compareTo(0) == 0) {
                lambda.set(i, new RealData(0.0001));
            }
            Data tmp = objectives.get(i).minus(idealPoint.get(i)).abs().div(lambda.get(i));
            if (tmp.compareTo(result) > 0) {
                result = tmp;
            }
        }
        return result;
    }

    /**
     * Update reference points (Hernandez Gomez and Coello Coello 2015)
     * 
     * @param zmin z^min
     * @param zmax z^max
     * @param P    current population
     */
    @SuppressWarnings("unchecked")
    protected void updateReferencePoint(List<Data> zmin, List<Data> zmax, ArrayList<S> P) {
        updatePoints(P);
        for (int index = 0; index < problem.getNumberOfObjectives(); index++) {
            if (zmin.get(index).compareTo(idealPoint.get(index)) < 0) {
                zmin.set(index, idealPoint.get(index).copy());
            }
        }
        record.add((ArrayList<Data>) nadirPoint.clone());
        ArrayList<Data> v = calculateVariance(record);

        Data max_v = v.get(0);
        for (int index = 0; index < problem.getNumberOfObjectives(); index++) {
            if (max_v.compareTo(v.get(index)) > 0) {
                max_v = v.get(index);
            }
        }
        if (max_v.compareTo(0.5) > 0) {
            for (int index = 0; index < problem.getNumberOfObjectives(); index++) {
                if (zmax.get(index).compareTo(nadirPoint.get(index)) < 0) {
                    zmax.set(index, nadirPoint.get(index).copy());
                }
            }
        } else {
            double epsilon = 0.001;
            boolean[] indexMarked = new boolean[problem.getNumberOfObjectives()];
            for (int index = 0; index < problem.getNumberOfObjectives(); index++) {
                if (zmax.get(index).minus(zmin.get(index)).abs().compareTo(epsilon) < 0) {
                    Data max = zmax.get(0);
                    for (Data value : zmax) {
                        if (value.compareTo(max) > 0) {
                            max = value;
                        }
                    }
                    zmax.set(index, max.copy());
                    indexMarked[index] = true;
                } else if (nadirPoint.get(index).compareTo(zmax.get(index)) > 0) {
                    zmax.set(index, nadirPoint.get(index).times(2).minus(zmax.get(index)));
                    indexMarked[index] = true;
                } else if (v.get(index).compareTo(0) == 0 && !indexMarked[index]) {
                    Data a = Data.getZeroByType(zmax.get(index));
                    for (ArrayList<Data> old : record) {
                        for (Data _data : old) {
                            if (a.compareTo(_data) < 0) {
                                a = _data;
                            }
                        }
                    }
                    zmax.set(index, (zmax.get(index).minus(a)).div(2));
                }
            }
        }
    }

    private ArrayList<Data> calculateVariance(ArrayList<ArrayList<Data>> rec) {
        ArrayList<Data> mean = new ArrayList<>();

        for (int index = 0; index < problem.getNumberOfObjectives(); index++) {
            Data acum = Data.getZeroByType(rec.get(0).get(0));
            for (ArrayList<Data> data : rec) {
                acum = acum.plus(data.get(index));
            }
            mean.add(acum.div(rec.size()));
        }
        ArrayList<Data> v = new ArrayList<>();
        for (int index = 0; index < problem.getNumberOfObjectives(); index++) {
            Data acum = Data.getZeroByType(rec.get(0).get(0));
            for (ArrayList<Data> data : rec) {
                acum = data.get(index).minus(mean.get(index)).pow(2).plus(acum);
            }
            v.add(acum.div(problem.getNumberOfObjectives()));
        }

        return v;
    }

    /**
     * Update Ideal and Nadir point using definition 6, 7
     * 
     * @param population from reference
     */
    protected void updatePoints(ArrayList<S> population) {
        for (S p : population) {
            for (int index = 0; index < problem.getNumberOfObjectives(); index++) {
                if (idealPoint.get(index).compareTo(p.getObjective(index)) > 0) {
                    idealPoint.set(index, p.getObjective(index).copy());
                }
                if (nadirPoint.get(index).compareTo(p.getObjective(index)) < 0) {
                    nadirPoint.set(index, p.getObjective(index).copy());
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public ArrayList<Data> getFNorm(S s) {
        return (ArrayList<Data>) s.getAttribute(NORMALIZE_KEY);
    }
}
