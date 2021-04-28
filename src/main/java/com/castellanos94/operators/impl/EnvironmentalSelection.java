package com.castellanos94.operators.impl;

import java.util.ArrayList;
import java.util.List;

import com.castellanos94.datatype.Data;
import com.castellanos94.operators.SelectionOperator;
import com.castellanos94.solutions.Solution;
import com.castellanos94.utils.ReferencePoint;
import com.castellanos94.utils.Tools;

public class EnvironmentalSelection<S extends Solution<?>> implements SelectionOperator<S> {
    private List<List<S>> fronts;
    private int solutionsToSelect;
    private List<ReferencePoint<S>> referencePoints;
    private int numberOfObjectives;
    protected ArrayList<S> parents;
    protected Data MaxValue, InfinityNegative, zeroValue, oneValue;

    public EnvironmentalSelection(List<List<S>> fronts, int solutionsToSelect, List<ReferencePoint<S>> referencePoints,
            int numberOfObjectives) {
        this.fronts = fronts;
        this.solutionsToSelect = solutionsToSelect;
        this.referencePoints = referencePoints;
        this.numberOfObjectives = numberOfObjectives;
        this.MaxValue = Data.initByRefType(fronts.get(0).get(0).getObjective(0), Double.MAX_VALUE);
        this.InfinityNegative = Data.initByRefType(fronts.get(0).get(0).getObjective(0), Double.NEGATIVE_INFINITY);
        this.zeroValue = Data.initByRefType(fronts.get(0).get(0).getObjective(0), 0.0);
        this.oneValue = Data.initByRefType(fronts.get(0).get(0).getObjective(0), 1.0);

    }

    public List<Data> translateObjectives(List<S> population) {
        List<Data> ideal_point;
        ideal_point = new ArrayList<>(numberOfObjectives);

        for (int f = 0; f < numberOfObjectives; f += 1) {
            Data minf = MaxValue;
            for (int i = 0; i < fronts.get(0).size(); i += 1) // min values must appear in the first front
            {
                if (minf.compareTo(fronts.get(0).get(i).getObjective(f)) < 0)
                    minf = fronts.get(0).get(i).getObjective(f);
                // minf = Math.min(minf, fronts.get(0).get(i).getObjective(f));
            }
            ideal_point.add(minf);

            for (List<S> list : fronts) {
                for (S s : list) {
                    if (f == 0) // in the first objective we create the vector of conv_objs
                        setAttribute(s, new ArrayList<Data>());
                    getAttribute(s).add(s.getObjective(f).minus(minf));
                }
            }
        }

        return ideal_point;
    }

    // ----------------------------------------------------------------------
    // ASF: Achivement Scalarization Function
    // I implement here a effcient version of it, which only receives the index
    // of the objective which uses 1.0; the rest will use 0.00001. This is
    // different to the one impelemented in C++
    // ----------------------------------------------------------------------
    private Data ASF(S s, int index) {
        Data max_ratio = InfinityNegative;
        for (int i = 0; i < s.getObjectives().size(); i++) {
            double weight = (index == i) ? 1.0 : 0.000001;
            Data tmp = s.getObjective(i).div(weight);
            if (tmp.compareTo(max_ratio) > 0) {
                max_ratio = tmp;
            }
            // max_ratio = Math.max(max_ratio, s.getObjective(i) / weight);
        }
        return max_ratio;
    }

    // ----------------------------------------------------------------------
    private List<S> findExtremePoints(List<S> population) {
        List<S> extremePoints = new ArrayList<>();
        S min_indv = null;
        for (int f = 0; f < numberOfObjectives; f += 1) {
            Data min_ASF = MaxValue;
            for (S s : fronts.get(0)) { // only consider the individuals in the first front
                Data asf = ASF(s, f);
                if (asf.compareTo(min_ASF) < 0) {
                    min_ASF = asf;
                    min_indv = s;
                }
            }

            extremePoints.add(min_indv);
        }
        return extremePoints;
    }

    public List<Data> guassianElimination(List<List<Data>> A, List<Data> b) {
        List<Data> x = new ArrayList<>();

        int N = A.size();
        for (int i = 0; i < N; i += 1) {
            A.get(i).add(b.get(i));
        }

        for (int base = 0; base < N - 1; base += 1) {
            for (int target = base + 1; target < N; target += 1) {
                Data ratio = A.get(target).get(base).div(A.get(base).get(base));
                for (int term = 0; term < A.get(base).size(); term += 1) {
                    A.get(target).set(term, A.get(target).get(term).minus(A.get(base).get(term).times(ratio)));
                }
            }
        }

        for (int i = 0; i < N; i++)
            x.add(zeroValue);

        for (int i = N - 1; i >= 0; i -= 1) {
            for (int known = i + 1; known < N; known += 1) {
                A.get(i).set(N, A.get(i).get(N).minus(A.get(i).get(known).times(x.get(known))));
            }
            x.set(i, A.get(i).get(N).div(A.get(i).get(i)));
        }
        return x;
    }

    public List<Data> constructHyperplane(List<S> population, List<S> extreme_points) {
        // Check whether there are duplicate extreme points.
        // This might happen but the original paper does not mention how to deal with
        // it.
        boolean duplicate = false;
        for (int i = 0; !duplicate && i < extreme_points.size(); i += 1) {
            for (int j = i + 1; !duplicate && j < extreme_points.size(); j += 1) {
                duplicate = extreme_points.get(i).equals(extreme_points.get(j));
            }
        }

        List<Data> intercepts = new ArrayList<>();

        if (duplicate) // cannot construct the unique hyperplane (this is a casual method to deal with
                       // the condition)
        {
            for (int f = 0; f < numberOfObjectives; f += 1) {
                // extreme_points[f] stands for the individual with the largest value of
                // objective f
                intercepts.add(extreme_points.get(f).getObjective(f));
            }
        } else {
            // Find the equation of the hyperplane
            List<Data> b = new ArrayList<>(); // (pop[0].objs().size(), 1.0);
            for (int i = 0; i < numberOfObjectives; i++)
                b.add(oneValue);

            List<List<Data>> A = new ArrayList<>();
            for (S s : extreme_points) {
                List<Data> aux = new ArrayList<>();
                for (int i = 0; i < numberOfObjectives; i++)
                    aux.add(s.getObjective(i));
                A.add(aux);
            }
            List<Data> x = guassianElimination(A, b);

            // Find intercepts
            for (int f = 0; f < numberOfObjectives; f += 1) {
                intercepts.add(oneValue.div(x.get(f)));

            }
        }
        return intercepts;
    }

    public void normalizeObjectives(List<S> population, List<Data> intercepts, List<Data> ideal_point) {
        for (int t = 0; t < fronts.size(); t += 1) {
            for (S s : fronts.get(t)) {

                for (int f = 0; f < numberOfObjectives; f++) {
                    List<Data> conv_obj = (List<Data>) getAttribute(s);
                    if (intercepts.get(f).minus(ideal_point.get(f)).abs().compareTo(10e10) > 0) {
                        // if (Math.abs(intercepts.get(f) - ideal_point.get(f)) > 10e-10) {
                        conv_obj.set(f, conv_obj.get(f).div(intercepts.get(f).minus(ideal_point.get(f))));
                    } else {
                        conv_obj.set(f, conv_obj.get(f).div(10e-10));
                    }

                }
            }
        }

    }

    public Data perpendicularDistance(List<Data> direction, List<Data> point) {
        Data numerator = zeroValue, denominator = zeroValue;
        for (int i = 0; i < direction.size(); i += 1) {
            numerator = numerator.plus(direction.get(i).times(point.get(i)));
            // numerator += direction.get(i) * point.get(i);
            denominator = denominator.plus(direction.get(i).pow(2));
            // denominator += Math.pow(direction.get(i), 2.0);
        }
        Data k = numerator.div(denominator);

        Data d = zeroValue;
        for (int i = 0; i < direction.size(); i += 1) {
            // d += Math.pow(k * direction.get(i) - point.get(i), 2.0);
            d = d.plus(k.times(direction.get(i).minus(point.get(i)).pow(2)));
        }
        return d.sqrt();
    }

    public void associate(List<S> population) {

        for (int t = 0; t < fronts.size(); t++) {
            for (S s : fronts.get(t)) {
                int min_rp = -1;
                Data min_dist = MaxValue;
                for (int r = 0; r < this.referencePoints.size(); r++) {
                    Data d = perpendicularDistance(this.referencePoints.get(r).position, (List<Data>) getAttribute(s));
                    if (d.compareTo(min_dist) < 0) {
                        min_dist = d;
                        min_rp = r;
                    }
                }
                if (t + 1 != fronts.size()) {
                    this.referencePoints.get(min_rp).AddMember();
                } else {
                    this.referencePoints.get(min_rp).AddPotentialMember(s, min_dist);
                }
            }
        }

    }

    int FindNicheReferencePoint() {
        // find the minimal cluster size
        int min_size = Integer.MAX_VALUE;
        for (ReferencePoint<S> referencePoint : this.referencePoints)
            min_size = Math.min(min_size, referencePoint.MemberSize());

        // find the reference points with the minimal cluster size Jmin
        List<Integer> min_rps = new ArrayList<>();

        for (int r = 0; r < this.referencePoints.size(); r += 1) {
            if (this.referencePoints.get(r).MemberSize() == min_size) {
                min_rps.add(r);
            }
        }
        // return a random reference point (j-bar)
        return min_rps.get((min_rps.size() > 1) ? Tools.getRandom().nextInt(min_rps.size() - 1) : 0);
    }

    // ----------------------------------------------------------------------
    // SelectClusterMember():
    //
    // Select a potential member (an individual in the front Fl) and associate
    // it with the reference point.
    //
    // Check the last two paragraphs in Section IV-E in the original paper.
    // ----------------------------------------------------------------------
    S SelectClusterMember(ReferencePoint<S> rp) {
        S chosen = null;
        if (rp.HasPotentialMember()) {
            if (rp.MemberSize() == 0) // currently has no member
            {
                chosen = rp.FindClosestMember();
            } else {
                chosen = rp.RandomMember();
            }
        }
        return chosen;
    }

    @Override
    /*
     * This method performs the environmental Selection indicated in the paper
     * describing NSGAIII
     */
    public Void execute(ArrayList<S> source) {
        // The comments show the C++ code

        // ---------- Steps 9-10 in Algorithm 1 ----------
        if (source.size() == this.solutionsToSelect) {
            parents = source;
            return null;
        }

        // ---------- Step 14 / Algorithm 2 ----------
        // vector<double> ideal_point = TranslateObjectives(&cur, fronts);
        List<Data> ideal_point = translateObjectives(source);
        List<S> extreme_points = findExtremePoints(source);
        List<Data> intercepts = constructHyperplane(source, extreme_points);

        normalizeObjectives(source, intercepts, ideal_point);
        // ---------- Step 15 / Algorithm 3, Step 16 ----------
        associate(source);

        // ---------- Step 17 / Algorithm 4 ----------
        while (source.size() < this.solutionsToSelect) {
            int min_rp = FindNicheReferencePoint();

            S chosen = SelectClusterMember(this.referencePoints.get(min_rp));
            if (chosen == null) // no potential member in Fl, disregard this reference point
            {
                this.referencePoints.remove(min_rp);
            } else {
                this.referencePoints.get(min_rp).AddMember();
                this.referencePoints.get(min_rp).RemovePotentialMember(chosen);
                source.add(chosen);
            }
        }
        parents = source;
        return null;
    }

    @Override
    public ArrayList<S> getParents() {
        return parents;
    }

    @Override
    public void setPopulationSize(int size) {
        System.out.println(size);
    }

    public void setAttribute(S solution, List<Data> value) {
        solution.getAttributes().put(getAttributeIdentifier(), value);
    }

    @SuppressWarnings("unchecked")
    public List<Data> getAttribute(S solution) {
        return (List<Data>) solution.getAttributes().getOrDefault(getAttributeIdentifier(), new ArrayList<>());
    }

    public String getAttributeIdentifier() {
        return this.getClass().getName();
    }

}