package com.castellanos94.algorithms.multi;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.castellanos94.algorithms.AbstractAlgorithm;
import com.castellanos94.datatype.Data;
import com.castellanos94.datatype.RealData;
import com.castellanos94.operators.impl.RepairBoundary;
import com.castellanos94.problems.Problem;
import com.castellanos94.solutions.DoubleSolution;
import com.castellanos94.utils.HeapSort;
import com.castellanos94.utils.ReferenceHyperplane;
import com.castellanos94.utils.Tools;
import com.google.common.math.BigIntegerMath;

/**
 * Falcón-Cardona, J.G., Coello Coello, C.A. A new indicator-based
 * many-objective ant colony optimizer for continuous search spaces. Swarm
 * Intell 11, 71–100 (2017). https://doi.org/10.1007/s11721-017-0133-x
 */
public class IMOACO_R<S extends DoubleSolution> extends AbstractAlgorithm<S> {
    public static String R2_Alpha_KEY = "R2-rank-alpha";
    public static String NORMALIZE_KEY = "imoaco-norm";
    public static String ANT_STD_KEY = "ant-std";
    public static String BEST_UTILITY_KEY = "u*";
    protected ArrayList<Data> idealPoint;
    protected ArrayList<Data> nadirPoint;
    protected ArrayList<ArrayList<Data>> record;
    protected final double q;
    protected final double xi;
    protected final int h;
    protected final int maxIterations;
    protected final int N;
    private List<Integer> kernelIntegerList;
    protected int[] mark;
    protected final int MAX_RECORD_SIZE;
    protected RepairBoundary repairBoundary;

    /**
     * 
     * @param problem       continuos problem, default repair for continuos problem
     *                      [0,1] var decision
     * @param maxIterations G_max
     * @param q             diversification process control parameter
     * @param xi            convergence rate control parameter
     * @param h             proportional parameter, using for the construction of
     *                      the simplex-lattice on the SLD in order to create set of
     *                      N convex weight vectors. N is equally used as the number
     *                      of ants
     * @see DoubleSolution
     * @see RepairBoundary
     */
    public IMOACO_R(Problem<S> problem, int maxIterations, double q, double xi, int h) {
        super(problem);
        this.maxIterations = maxIterations;
        this.q = q;
        this.xi = xi;
        this.h = h;
        this.N = BigIntegerMath.factorial(problem.getNumberOfObjectives() + h - 1).divide(
                BigIntegerMath.factorial(h).multiply(BigIntegerMath.factorial(problem.getNumberOfObjectives() - 1)))
                .intValueExact();
        this.kernelIntegerList = IntStream.range(0, N).boxed().collect(Collectors.toList());
        this.mark = new int[problem.getNumberOfObjectives()];
        this.MAX_RECORD_SIZE = 5;
        this.repairBoundary = new RepairBoundary();

    }

    /**
     * 
     * @param problem       continuos problem, default repair for continuos problem
     *                      [0,1] var decision
     * @param maxIterations G_max
     * @param N             population size
     * @param q             diversification process control parameter
     * @param xi            convergence rate control parameter
     * @param h             proportional parameter, using for the construction of
     *                      the simplex-lattice on the SLD in order to create set of
     *                      N convex weight vectors. N is equally used as the number
     *                      of ants
     * @see DoubleSolution
     * @see RepairBoundary
     */
    public IMOACO_R(Problem<S> problem, int maxIterations, int N, double q, double xi, int h) {
        super(problem);
        this.maxIterations = maxIterations;
        this.q = q;
        this.xi = xi;
        this.h = h;
        this.N = N;
        this.kernelIntegerList = IntStream.range(0, N).boxed().collect(Collectors.toList());
        this.mark = new int[problem.getNumberOfObjectives()];
        this.MAX_RECORD_SIZE = 5;
        this.repairBoundary = new RepairBoundary();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute() {
        this.init_time = System.currentTimeMillis();
        ArrayList<ArrayList<Data>> LAMBDA = generateWeight();

        Comparator<S> cmp = (a, b) -> Integer.compare(a.getRank(), b.getRank());
        cmp = cmp.thenComparing((a, b) -> {
            Data ua = (Data) a.getAttribute(BEST_UTILITY_KEY);
            Data ub = (Data) b.getAttribute(BEST_UTILITY_KEY);
            return ua.compareTo(ub);
        });
        cmp.thenComparing((a, b) -> {
            Data d1 = Tools.NORML2(a.getObjectives());
            Data d2 = Tools.NORML2(b.getObjectives());
            return d1.compareTo(d2);
        });
        HeapSort<S> sorted3Criterial = new HeapSort<>(cmp);
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
        saveRegisterInRecord(nadirPoint);
        R2Ranking(solutions, idealPoint, LAMBDA);
        this.solutions.sort((a, b) -> Integer.compare(a.getRank(), b.getRank()));
        ArrayList<Data> zmin = (ArrayList<Data>) idealPoint.clone();
        ArrayList<Data> zmax = (ArrayList<Data>) nadirPoint.clone();
        for (int iteration = 0; iteration < maxIterations; iteration++) {
            // The search engine method creates a new solution for each ant and returns all
            // the solutions made.
            ArrayList<S> ns = searchEngine(solutions);
            updateReferencePoint(zmin, zmax, ns, iteration);
            ArrayList<S> psi = new ArrayList<>(solutions);
            psi.addAll(ns);
            normalize(psi, idealPoint, nadirPoint);
            R2Ranking(psi, idealPoint, LAMBDA);
            // Ordenar PSI en forma creciente con respecto a los criterios (1) rank, (2) u*
            // y (3) norma L_2
            sorted3Criterial.sort(psi);
            // Copiar en Tau los primeros elementos de psi
            for (int i = 0; i < this.N; i++) {
                this.solutions.set(i, (S) psi.get(i).copy());
            }
            R2Ranking(solutions, idealPoint, LAMBDA);
        }
        this.computeTime = System.currentTimeMillis() - this.init_time;
    }

    /**
     * This method is based on the implementation of iMOACOR: a new indicator-based
     * multi-target ant colony optimization algorithm for continuous search spaces
     * MSc. Jesús Guillermo Falcón Cardona. Source Code of iMOACOR v1.0
     * http://computacion.cs.cinvestav.mx/~jfalcon/iMOACOR/imoacor.html
     * 
     * @param solutions
     * @return
     */
    protected ArrayList<S> searchEngine(ArrayList<S> solutions) {
        ArrayList<S> rs = new ArrayList<>(this.N);
        // filled with empty solutions
        for (int i = 0; i < this.N; i++) {
            rs.add(this.problem.getEmptySolution());
        }
        // calculate weights
        double[] weight = new double[this.N];
        double totalWeight = 0.0;
        for (int index = 0; index < this.N; index++) {
            S s_k = solutions.get(index);
            // w_j
            weight[index] = (Math.exp(-Math.pow(s_k.getRank() - 1, 2) / (2 * this.q * this.q * this.N * this.N)))
                    / (this.q * this.N * Math.sqrt(2 * Math.PI));
            totalWeight += weight[index];
        }
        // calculate probabilities
        double[] probabilities = new double[this.N];
        for (int index = 0; index < this.N; index++) {
            probabilities[index] = weight[index] / totalWeight;
        }
        // Ants
        int[] antKernelIndex = chooseGaussianKernel(probabilities);

        for (int i = 0; i < problem.getNumberOfDecisionVars(); i++) {
            calculateStdDev(solutions, i);
            antSampling(rs, solutions, antKernelIndex, i);
        }
        for (S ant : rs) {
            this.repairBoundary.execute(ant);
            problem.evaluate(ant);
            problem.evaluateConstraint(ant);
        }
        return rs;
    }

    protected void antSampling(ArrayList<S> ants, ArrayList<S> pheromones, int[] kernelIndex, int index) {
        for (int i = 0; i < this.N; i++) {
            int selectedKernel = kernelIndex[i];
            S ant = ants.get(i);
            ant.setVariable(index, Normal(pheromones.get(selectedKernel).getVariable(index),
                    (double) pheromones.get(selectedKernel).getAttribute(ANT_STD_KEY), index));
        }
    }

    /**
     * Box Muller method
     * 
     * @param mean
     * @param stdDev
     * @param index
     * @return
     */
    private double Normal(Double mean, double stdDev, int index) {
        double U1, U2, Z, X;
        do {
            do {
                U1 = Tools.getRandom().nextDouble();
            } while (U1 == 0.0 || U1 == 1.0);

            do {
                U2 = Tools.getRandom().nextDouble();
            } while (U2 == 0.0 || U1 == 1.0);

            if (Tools.flip(0.5))
                // Standard random variable (media = 0, desvStd = 1)
                Z = Math.sqrt(-2.0 * Math.log(U1)) * Math.cos(2.0 * Math.PI * U2);
            else
                // Standard random variable (media = 0, desvStd = 1)
                Z = Math.sqrt(-2.0 * Math.log(U1)) * Math.sin(2.0 * Math.PI * U2);
            /*
             * Transformando la variable aleatoria normal estándar Z, a una variable
             * aleatoria normal con media y desviación tomadas del archivo de soluciones
             * previamente.
             */
            X = Z * stdDev + mean;

        } while (problem.getLowerBound()[index].compareTo(X) > 0 || problem.getUpperBound()[index].compareTo(X) < 0);
        return X;
    }

    protected void calculateStdDev(ArrayList<S> solutions, int index) {
        for (int i = 0; i < this.N; i++) {
            double std = 0;
            for (int j = 0; j < this.N; j++) {
                std += Math.abs(solutions.get(j).getVariable(index) - solutions.get(i).getVariable(index));
            }
            std *= this.xi / (((double) this.N) - 1.0);
            solutions.get(i).setAttribute(ANT_STD_KEY, std);
        }
    }

    protected int[] chooseGaussianKernel(double[] probabilities) {
        int[] kernelIndex = new int[this.N];
        for (int i = 0; i < kernelIndex.length; i++) {
            kernelIndex[i] = rouletteWheelSelection(probabilities);
        }
        return kernelIndex;
    }

    private int rouletteWheelSelection(double[] probabilities) {
        Tools.shuffle(kernelIntegerList);
        double rand = Tools.getRandom().nextDouble();
        double sum = 0;
        for (int i = 0; i < probabilities.length; i++) {
            sum += probabilities[kernelIntegerList.get(i)];
            if (sum >= rand) {
                return kernelIntegerList.get(i);
            }
        }
        return 0;
    }

    protected void normalize(ArrayList<S> solutions, ArrayList<Data> zmin, ArrayList<Data> zmax) {
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
        if (problem.getNumberOfObjectives() <= 5) {
            ReferenceHyperplane<S> referenceHyperplane = new ReferenceHyperplane<>(problem.getNumberOfObjectives(), h);
            referenceHyperplane.execute();
            ArrayList<ArrayList<Data>> data = referenceHyperplane.transformToData();
            return data;
        }

        ReferenceHyperplane<S> referenceHyperplane = new ReferenceHyperplane<>(problem.getNumberOfObjectives(), 3);
        referenceHyperplane.execute();
        ArrayList<ArrayList<Data>> data = referenceHyperplane.transformToData();
        ReferenceHyperplane<S> referenceHyperplane2 = new ReferenceHyperplane<>(problem.getNumberOfObjectives(), 2);
        referenceHyperplane2.execute();
        data.addAll(referenceHyperplane2.transformToData());
        return data;
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
        Comparator<S> comparator = (a, b) -> {
            Data alpha_a = (Data) a.getAttribute(R2_Alpha_KEY);
            Data alpha_b = (Data) b.getAttribute(R2_Alpha_KEY);
            return alpha_a.compareTo(alpha_b);
        };
        comparator = comparator.thenComparing((a, b) -> {
            Data d1 = Tools.NORML2(a.getObjectives());
            Data d2 = Tools.NORML2(b.getObjectives());
            return d1.compareTo(d2);
        });
        HeapSort<S> sorter = new HeapSort<>(comparator);

        for (int i = 0; i < LAMBDA.size(); i++) {
            List<Data> lambda = LAMBDA.get(i);
            for (S p : P) {
                p.setAttribute(R2_Alpha_KEY, ASF(getFNorm(p), idealPoint, lambda));
            }

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
    protected void updateReferencePoint(ArrayList<Data> zmin, ArrayList<Data> zmax, ArrayList<S> P, int gen) {
        updatePoints(zmin, zmax, P);
        for (int index = 0; index < problem.getNumberOfObjectives(); index++) {
            if (zmin.get(index).compareTo(idealPoint.get(index)) < 0) {
                idealPoint.set(index, zmin.get(index).copy());
            }
        }
        saveRegisterInRecord(zmax);
        if (gen >= this.MAX_RECORD_SIZE - 1) {
            ArrayList<Data> mu = calculateMean(record);
            ArrayList<Data> v = calculateVariance(record, mu);
            double epsilon = 0.001;
            for (int index = 0; index < problem.getNumberOfObjectives(); index++) {
                Data var = v.get(index);
                if (var.compareTo(0.5) > 0) {
                    Data max = getMaxValueFromVector(zmax);
                    for (int j = 0; j < problem.getNumberOfObjectives(); j++) {
                        nadirPoint.set(j, max);
                    }
                    break;
                } else {
                    if (nadirPoint.get(index).minus(idealPoint.get(index)).abs().compareTo(epsilon) < 0) {
                        nadirPoint.set(index, getMaxValueFromVector(nadirPoint));
                        mark[index] = MAX_RECORD_SIZE;
                    } else if (zmax.get(index).compareTo(nadirPoint.get(index)) > 0) {
                        nadirPoint.set(index, zmax.get(index).plus(zmax.get(index).minus(nadirPoint.get(index))));
                    } else if (var.compareTo(0) == 0
                            || (zmax.get(index).minus(mu.get(index)).compareTo(epsilon) > 0 && mark[index] == 0)) {
                        Data a = Data.getZeroByType(zmax.get(index));
                        for (ArrayList<Data> old : record) {
                            for (Data _data : old) {
                                if (a.compareTo(_data) < 0) {
                                    a = _data;
                                }
                            }
                        }
                        nadirPoint.set(index, (nadirPoint.get(index).minus(a)).div(2));
                    }
                }
                if (mark[index] > 0) {
                    mark[index]--;
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected void saveRegisterInRecord(ArrayList<Data> zmax) {
        if (this.record.size() < MAX_RECORD_SIZE) {
            this.record.add((ArrayList<Data>) zmax.clone());
        } else {
            this.record.remove(0);
            this.record.add((ArrayList<Data>) zmax.clone());
        }

    }

    protected Data getMaxValueFromVector(ArrayList<Data> zmax) {
        Data rs = zmax.get(0);
        for (int i = 0; i < zmax.size(); i++) {
            if (rs.compareTo(zmax.get(i)) < 0) {
                rs = zmax.get(i);
            }
        }
        return rs;
    }

    protected ArrayList<Data> calculateMean(ArrayList<ArrayList<Data>> rec) {
        ArrayList<Data> mean = new ArrayList<>();

        for (int index = 0; index < problem.getNumberOfObjectives(); index++) {
            Data acum = Data.getZeroByType(rec.get(0).get(0));
            for (ArrayList<Data> data : rec) {
                acum = acum.plus(data.get(index));
            }
            mean.add(acum.div(rec.size()));
        }
        return mean;
    }

    private ArrayList<Data> calculateVariance(ArrayList<ArrayList<Data>> rec, ArrayList<Data> mean) {

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
    protected void updatePoints(List<Data> zmin, List<Data> zmax, ArrayList<S> population) {
        for (S p : population) {
            for (int index = 0; index < problem.getNumberOfObjectives(); index++) {
                if (zmin.get(index).compareTo(p.getObjective(index)) > 0) {
                    zmin.set(index, p.getObjective(index).copy());
                }
                if (zmax.get(index).compareTo(p.getObjective(index)) < 0) {
                    zmax.set(index, p.getObjective(index).copy());
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected ArrayList<Data> getFNorm(S s) {
        return (ArrayList<Data>) s.getAttribute(NORMALIZE_KEY);
    }

    @Override
    public String toString() {
        return "IMOACO_R [N=" + N + ", h=" + h + ", maxIterations=" + maxIterations + ", q=" + q + ", xi=" + xi
                + ", MAX RECORD SIZE=" + MAX_RECORD_SIZE + "]";
    }

    @Override
    public IMOACO_R<S> copy() {
        return new IMOACO_R<>(problem, maxIterations, N, q, xi, h);
    }

}
