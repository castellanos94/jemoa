package com.castellanos94.algorithms.multi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import com.castellanos94.algorithms.AbstractEvolutionaryAlgorithm;
import com.castellanos94.components.Ranking;
import com.castellanos94.datatype.Data;
import com.castellanos94.datatype.IntegerData;
import com.castellanos94.datatype.RealData;
import com.castellanos94.operators.CrossoverOperator;
import com.castellanos94.operators.MutationOperator;
import com.castellanos94.operators.SelectionOperator;
import com.castellanos94.problems.Problem;
import com.castellanos94.solutions.Solution;
import com.castellanos94.utils.ReferenceHyperplane;
import com.castellanos94.utils.Tools;

/**
 * θ-NSGA-III implemented : Yuan, Y., Xu, H., & Wang, B. (2014). An improved
 * NSGA-III procedure for evolutionary many-objective optimization. Proceedings
 * of the 2014 Conference on Genetic and Evolutionary Computation - GECCO ’14.
 * doi:10.1145/2576768.2598342.
 */
public class Theta_NSGA_III<S extends Solution<?>> extends AbstractEvolutionaryAlgorithm<S> {
    protected int maxIterations;
    protected int currenIteration;
    protected int numberOfReferencePoints;
    protected ArrayList<ArrayList<Data>> references;
    protected ReferenceHyperplane<S> referenceHyperplane;

    protected S idealPoint;
    protected S nadirPoint;
    private static String NORMALIZE_OBJETIVES_KEY = "theta-normalize-objectives";
    public static String DJ1_KEY = "dj1_theta";
    public static String DJ2_KEY = "dj2_theta";
    public static String CLUSTER_KEY = "FROM_CLUSTER";
    public double theta = 5;

    public Theta_NSGA_III(Problem<S> problem, int populationSize, int maxIterations,
            SelectionOperator<S> selectionOperator, CrossoverOperator<S> crossoverOperator,
            MutationOperator<S> mutationOperator, int numberOfReferencePoints) {
        super(problem);
        this.maxIterations = maxIterations;
        this.selectionOperator = selectionOperator;
        this.crossoverOperator = crossoverOperator;
        this.mutationOperator = mutationOperator;
        this.populationSize = populationSize;

        this.numberOfReferencePoints = numberOfReferencePoints;
        this.references = normalizePoint(createReferencePoint());
        this.referenceHyperplane = new ReferenceHyperplane<>(problem.getNumberOfObjectives(), 12);
        this.referenceHyperplane.execute();

    }

    private ArrayList<ArrayList<Data>> normalizePoint(ArrayList<ArrayList<Data>> points) {
        for (int i = 0; i < this.problem.getNumberOfObjectives(); i++) {
            Data min = points.get(0).get(i);
            Data max = points.get(0).get(i);
            for (int j = 0; j < points.size(); j++) {
                if (min.compareTo(points.get(j).get(i)) > 0) {
                    min = points.get(j).get(i);
                }
                if (max.compareTo(points.get(j).get(i)) < 0) {
                    max = points.get(j).get(i);
                }
            }
            for (int j = 0; j < points.size(); j++) {
                points.get(j).set(i, (points.get(j).get(i).minus(min)).div(max.minus(min)));
            }
        }
        return points;
    }

    @Override
    protected void updateProgress() {
        currenIteration++;
    }

    @Override
    protected ArrayList<S> reproduction(ArrayList<S> parents) {
        ArrayList<S> offspring = new ArrayList<>();
        for (int i = 0; i < populationSize; i++) {
            ArrayList<S> p = new ArrayList<>();
            p.add(parents.get(i++));
            p.add(parents.get((i < parents.size()) ? i : 0));
            offspring.addAll(crossoverOperator.execute(p));

        }
        for (int i = 0; i < offspring.size(); i++) {
            offspring.set(i, mutationOperator.execute(offspring.get(i)));
            problem.evaluate(offspring.get(i));
            problem.evaluateConstraint(offspring.get(i));
        }
        return offspring;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected ArrayList<S> replacement(ArrayList<S> population, ArrayList<S> offspring) {

        updateIdealPoint(offspring);

        ArrayList<S> Rt = new ArrayList<>(population);
        Rt.addAll(offspring);
        Rt = new ArrayList<>(Rt.stream().distinct().collect(Collectors.toList()));
        while (Rt.size() < populationSize) {
            S r = problem.randomSolution();
            problem.evaluate(r);
            problem.evaluateConstraint(r);
            Rt.add(r);
        }
        normalize(Rt);
        ArrayList<ArrayList<S>> clusters = clustering(Rt);
        ThetaNonDominatedSort<S> ranking = new ThetaNonDominatedSort<>(theta, clusters);
        ranking.computeRanking(Rt);
        ArrayList<S> Pt = new ArrayList<>();
        int indexFront = 0;
        ArrayList<ArrayList<S>> fronts = new ArrayList<>();
        for (; indexFront < ranking.getNumberOfSubFronts(); indexFront++) {
            fronts.add(ranking.getSubFront(indexFront));
            if (Pt.size() + ranking.getSubFront(indexFront).size() <= populationSize) {
                for (S solution : ranking.getSubFront(indexFront)) {
                    Pt.add((S) solution.copy());
                }
            } else {
                break;
            }
        }
        while (Pt.size() < populationSize) {
            ArrayList<S> subFront = ranking.getSubFront(indexFront);
            Collections.shuffle(subFront);
            for (S s : subFront) {
                if (Pt.size() < this.populationSize) {
                    Pt.add(s);
                } else {
                    break;
                }
            }
            indexFront++;
        }

        return Pt;
    }

    private ArrayList<ArrayList<S>> clustering(ArrayList<S> rt) {
        ArrayList<ArrayList<S>> clusters = new ArrayList<>();
        for (int i = 0; i < this.numberOfReferencePoints; i++) {
            clusters.add(new ArrayList<>());
        }
        // Algorithm 4: process of clustering
        for (S solution : rt) {
            int k = 0;
            // Data min = calcualteDJ2(solution,
            // this.referenceHyperplane.get(0).getPoint());
            Data min = calcualteDJ2(solution, this.references.get(0));
            for (int i = 1; i < this.numberOfReferencePoints; i++) {
                // Data dj2 = calcualteDJ2(solution,
                // this.referenceHyperplane.get(i).getPoint());
                Data dj2 = calcualteDJ2(solution, this.references.get(i));
                if (dj2.compareTo(min) < 0) {
                    min = dj2;
                    k = i;
                }
            }
            solution.setAttribute(CLUSTER_KEY, k);
            clusters.get(k).add(solution);
        }
        return clusters;
    }

    @SuppressWarnings("unchecked")
    private Data calcualteDJ2(S solution, List<Data> lambda_j) {
        List<Data> x = (List<Data>) solution.getAttribute(NORMALIZE_OBJETIVES_KEY);
        // Let dj1(x) be the distance between the origin and u
        Data dj1 = null;
        for (int objective = 0; objective < lambda_j.size(); objective++) {
            if (lambda_j.get(objective).compareTo(0) != 0) {
                if (dj1 == null) {
                    dj1 = ((x.get(objective).times(lambda_j.get(objective))).div(lambda_j.get(objective).abs())).abs();
                } else {
                    dj1 = dj1
                            .plus(((x.get(objective).times(lambda_j.get(objective))).div(lambda_j.get(objective).abs()))
                                    .abs());
                }
            }
        }
        // Let dj2 the perpendicular distance between f(x) and L
        Data dj2 = null;
        for (int objective = 0; objective < lambda_j.size(); objective++) {
            if (lambda_j.get(objective).compareTo(0) != 0) {
                if (dj2 == null) {
                    dj2 = (x.get(objective)
                            .minus(dj1.times(lambda_j.get(objective).div(lambda_j.get(objective).abs())))).abs();
                } else {
                    dj2 = dj2.plus((x.get(objective)
                            .minus(dj1.times(lambda_j.get(objective).div(lambda_j.get(objective).abs())))).abs());
                }
            }
        }
        solution.setAttribute(DJ1_KEY, dj1);
        solution.setAttribute(DJ2_KEY, dj2);
        return dj2;
    }

    @SuppressWarnings("unchecked")
    private void normalize(ArrayList<S> rt) {

        nadirPoint = (S) idealPoint.copy();

        for (int i = 0; i < this.problem.getNumberOfObjectives(); i++) {
            for (int j = 1; j < rt.size(); j++) {
                if (nadirPoint.getObjective(i).compareTo(rt.get(j).getObjective(i)) < 0) {
                    nadirPoint.setObjective(i, rt.get(j).getObjective(i));
                }
            }
        }
        for (S child : rt) {
            List<Data> objectives = child.getObjectives();
            List<Data> normalizedObjectives = new ArrayList<>();
            for (int i = 0; i < objectives.size(); i++) {
                normalizedObjectives.add((objectives.get(i).minus(idealPoint.getObjective(i)))
                        .div(nadirPoint.getObjective(i).minus(idealPoint.getObjective(i))));
            }
            child.setAttribute(NORMALIZE_OBJETIVES_KEY, normalizedObjectives);
        }
    }

    @SuppressWarnings("unchecked")
    protected void updateIdealPoint(ArrayList<S> offspring) {
        if (idealPoint == null) {
            idealPoint = (S) solutions.get(0).copy();
            idealPoint.setPenalties(new IntegerData(0));
            updateIdealPoint(solutions);
        }

        for (int i = 0; i < this.problem.getNumberOfObjectives(); i++) {
            for (int j = 1; j < offspring.size(); j++) {
                if (idealPoint.getObjective(i).compareTo(offspring.get(j).getObjective(i)) > 0) {
                    idealPoint.setObjective(i, offspring.get(j).getObjective(i));
                }
            }
        }

    }

    @Override
    protected boolean isStoppingCriteriaReached() {
        return currenIteration < maxIterations;
    }

    private ArrayList<ArrayList<Data>> createReferencePoint() {
        ArrayList<ArrayList<Data>> points = new ArrayList<>();
        for (int i = 0; i < this.numberOfReferencePoints; i++) {
            double s = 0;
            ArrayList<Data> point = new ArrayList<>();
            for (int j = 0; j < this.problem.getNumberOfObjectives(); j++) {
                if (j < this.problem.getNumberOfObjectives() - 1) {
                    double rand = Tools.getRandom().nextDouble();
                    double lambda_j = (1 - s) * (1 - Math.pow(rand,
                            1.0 / (this.problem.getNumberOfObjectives() - this.numberOfReferencePoints)));
                    point.add(new RealData(lambda_j));
                    s += lambda_j;
                } else {
                    point.add(new RealData(1 - s));
                }
            }
            points.add(point);
        }
        return points;
    }

    @Override
    public String toString() {
        return "Theta-NSGAIII [pop_size=" + populationSize + ", maxIterations=" + maxIterations + ", referencePoints="
                + numberOfReferencePoints + ", theta=" + theta + "]";
    }

    public static class ThetaNonDominatedSort<S extends Solution<?>> implements Ranking<S> {
        ArrayList<ArrayList<S>> fronts;
        private double theta;

        public ThetaNonDominatedSort(double theta, ArrayList<ArrayList<S>> clusters) {
            this.fronts = new ArrayList<>();
            this.theta = theta;
        }

        /**
         * Given two solutions x, y in omega is said to theta-dominate y denote by x <
         * y, if x in Cj, y in Cj and Fj(x) < Fj(y)
         * 
         * @param x
         * @param y
         * @param cluster
         * @return -1 x-dom, 0 equals, 1 y-dom , 2 no clustering
         */
        public int thetaDominance(S x, S y) {
            int cx = (int) x.getAttribute(CLUSTER_KEY);
            int cy = (int) y.getAttribute(CLUSTER_KEY);
            if (cx == cy) {
                Data fjx = calcualteFj(x);
                Data fjy = calcualteFj(y);
                if (fjx.compareTo(fjy) < 0) {
                    return -1;
                }
                if (fjy.compareTo(fjx) < 0) {
                    return 1;
                }
                return 0;
            }
            return 2;
        }

        /**
         * Function similar to penalty-based intersection. Theta is a pedefined penalty
         * parameter.
         * 
         * @param x
         * @return value
         */
        private Data calcualteFj(S x) {
            Data dj1 = (Data) x.getAttribute(DJ1_KEY);
            Data dj2 = (Data) x.getAttribute(DJ2_KEY);
            return dj1.plus(dj2.times(theta));
        }

        @Override
        public void computeRanking(ArrayList<S> population) {
            this.fronts = new ArrayList<>();
            ArrayList<ArrayList<Integer>> dominate_me = new ArrayList<>();

            for (int i = 0; i < population.size(); i++) {
                dominate_me.add(new ArrayList<>());
                fronts.add(new ArrayList<>());
            }
            for (int i = 0; i < population.size() - 1; i++) {
                for (int j = 1; j < population.size(); j++) {
                    // if (i != j && !population.get(i).equals(population.get(j))) {
                    int value = thetaDominance(population.get(i), population.get(j));
                    if (value == -1 && !dominate_me.get(j).contains(i)) {
                        dominate_me.get(j).add(i);
                    } else if (value == 1 && !dominate_me.get(i).contains(j)) {
                        dominate_me.get(i).add(j);
                    }
                    // }
                }
            }
            int i = 0;
            Iterator<ArrayList<Integer>> iter = dominate_me.iterator();

            ArrayList<Integer> toRemove = new ArrayList<>();
            int min = getMinDom(dominate_me);
            i = 0;
            while (iter.hasNext()) {
                ArrayList<Integer> dom = iter.next();
                if (dom.size() == min) {
                    fronts.get(0).add(population.get(i));
                    toRemove.add(i);
                }
                i++;
            }
            for (Integer integer : toRemove) {
                solutionRemove(integer, dominate_me);
            }
            for (int j = 1; j < fronts.size(); j++) {
                ArrayList<S> currentFront = fronts.get(j);
                i = 0;
                min = getMinDom(dominate_me);
                for (ArrayList<Integer> dom_me : dominate_me) {
                    if (!toRemove.contains(i) && dom_me.size() == min) {
                        currentFront.add(population.get(i));
                        toRemove.add(i);
                    }
                    i++;
                }
                for (Integer integer : toRemove) {
                    solutionRemove(integer, dominate_me);
                }
            }
            setRanking();
        }

        private void setRanking() {
            fronts.removeIf(front -> front.size() == 0);

            for (int j = 0; j < fronts.size(); j++) {
                ArrayList<S> front = fronts.get(j);
                for (S solution : front) {
                    solution.setRank(j);
                }
            }
        }

        private int getMinDom(ArrayList<ArrayList<Integer>> dominate_me) {
            int min = Integer.MAX_VALUE;
            for (ArrayList<Integer> arrayList : dominate_me) {
                if (arrayList.size() < min) {
                    min = arrayList.size();
                }
            }
            return min;
        }

        private void solutionRemove(Integer i, ArrayList<ArrayList<Integer>> dominate_me) {
            for (ArrayList<Integer> arrayList : dominate_me) {
                if (arrayList.contains(i)) {
                    arrayList.remove(i);
                }
            }
        }

        @Override
        public ArrayList<S> getSubFront(int index) {
            return fronts.get(index);
        }

        @Override
        public int getNumberOfSubFronts() {
            return fronts.size();
        }

    }

    @Override
    public Theta_NSGA_III<S> copy() {
        return new Theta_NSGA_III<>(problem, populationSize, maxIterations, selectionOperator, crossoverOperator,
                mutationOperator, numberOfReferencePoints);
    }
}