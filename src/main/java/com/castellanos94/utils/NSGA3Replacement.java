package com.castellanos94.utils;

import java.util.ArrayList;
import java.util.List;

import com.castellanos94.datatype.Data;
import com.castellanos94.operators.SelectionOperator;
import com.castellanos94.solutions.Solution;
import com.castellanos94.utils.ReferenceHyperplane.ReferencePointC;

import org.apache.commons.math3.util.Precision;

public class NSGA3Replacement implements SelectionOperator {
    private ReferenceHyperplane referencePoints;
    private int number_of_objectives;
    private int pop_size;
    private ArrayList<ArrayList<Solution>> fronts;
    private final double epsilon = 1e-6;
    private Data MAX_VALUE, MIN_VALUE, ZERO_VALUE, ONE_VALUE;
    private static final String OBJECTIVES_TRANSLATED = "objectivos_transladados_nsga3";
    private ArrayList<Solution> parents;

    public NSGA3Replacement(ArrayList<ArrayList<Solution>> fronts, ReferenceHyperplane referencePoints,
            int number_of_objectives, int pop_size) {
        this.fronts = fronts;
        this.number_of_objectives = number_of_objectives;
        this.pop_size = pop_size;
        this.referencePoints = referencePoints;
        MAX_VALUE = Data.initByRefType(fronts.get(0).get(0).getObjective(0), Double.MAX_VALUE);
        MIN_VALUE = Data.initByRefType(fronts.get(0).get(0).getObjective(0), Double.MIN_VALUE);
        ZERO_VALUE = Data.initByRefType(fronts.get(0).get(0).getObjective(0), 0);
        ONE_VALUE = Data.initByRefType(fronts.get(0).get(0).getObjective(0), 1);
    }

    public ArrayList<Data> translateObjectives(ArrayList<Solution> population) {
        ArrayList<Data> ideal_point;
        ideal_point = new ArrayList<>(number_of_objectives);

        for (int f = 0; f < number_of_objectives; f += 1) {
            Data minf = MAX_VALUE;
            for (int i = 0; i < fronts.get(0).size(); i += 1) // min values must appear in the first front
            {
                if (minf.compareTo(fronts.get(0).get(i).getObjective(f)) >0)
                    minf = fronts.get(0).get(i).getObjective(f);
            }
            ideal_point.add(minf);

            for (ArrayList<Solution> list : fronts) {
                for (Solution s : list) {
                    if (f == 0) // in the first objective we create the vector of conv_objs
                        setAttribute(s, new ArrayList<Data>());
                    Data tmp = s.getObjective(f).minus(minf);
                    if (tmp.compareTo(1e-3) < 0)
                        try {
                            tmp = (Data) ZERO_VALUE.clone();
                        } catch (CloneNotSupportedException e) {
                            e.printStackTrace();
                        }

                    getAttribute(s).add(tmp);
                }
            }
        }

        return ideal_point;
    }

    private ArrayList<Data> getAttribute(Solution s) {
        return (ArrayList<Data>) s.getAttribute(OBJECTIVES_TRANSLATED);
    }

    private void setAttribute(Solution s, ArrayList<Data> arrayList) {
        s.setAttribute(OBJECTIVES_TRANSLATED, arrayList);
    }

    // ----------------------------------------------------------------------
    // ASF: Achivement Scalarization Function
    // I implement here a effcient version of it, which only receives the index
    // of the objective which uses 1.0; the rest will use 0.00001. This is
    // different to the one impelemented in C++
    // ----------------------------------------------------------------------
    private Data ASF(Solution s, int index) {
        Data max_ratio = MIN_VALUE;
        for (int i = 0; i < s.getObjectives().size(); i++) {
            double weight = (index == i) ? 1.0 : epsilon;
            Data tmp = s.getObjective(i).div(weight);
            if (tmp.compareTo(max_ratio) > 0) {
                max_ratio = tmp;
            }
        }
        return max_ratio;
    }

    private ArrayList<Solution> findExtremePoints(ArrayList<Solution> population) {
        ArrayList<Solution> extremePoints = new ArrayList<>();
        Solution min_indv = null;
        for (int f = 0; f < number_of_objectives; f += 1) {
            Data min_ASF = MAX_VALUE;
            for (Solution s : fronts.get(0)) { // only consider the individuals in the first front
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

    public ArrayList<Data> guassianElimination(ArrayList<ArrayList<Data>> A, ArrayList<Data> b) {
        ArrayList<Data> x = new ArrayList<>();

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
            x.add(ZERO_VALUE);

        for (int i = N - 1; i >= 0; i -= 1) {
            for (int known = i + 1; known < N; known += 1) {
                A.get(i).set(N, A.get(i).get(N).minus(A.get(i).get(known).times(x.get(known))));
            }
            x.set(i, A.get(i).get(N).div(A.get(i).get(i)));
        }
        return x;
    }

    public ArrayList<Data> constructHyperplane(ArrayList<Solution> population, ArrayList<Solution> extreme_points) {
        // Check whether there are duplicate extreme points.
        // This might happen but the original paper does not mention how to deal with
        // it.
        boolean duplicate = false;
        for (int i = 0; !duplicate && i < extreme_points.size(); i += 1) {
            for (int j = i + 1; !duplicate && j < extreme_points.size(); j += 1) {
                duplicate = extreme_points.get(i).equals(extreme_points.get(j));
            }
        }

        ArrayList<Data> intercepts = new ArrayList<>();

        if (duplicate) // cannot construct the unique hyperplane (this is a casual method to deal with
                       // the condition)
        {
            for (int f = 0; f < number_of_objectives; f += 1) {
                // extreme_points[f] stands for the individual with the largest value of
                // objective f
                intercepts.add(extreme_points.get(f).getObjective(f));
            }
        } else {
            // Find the equation of the hyperplane
            ArrayList<Data> b = new ArrayList<>(); // (pop[0].objs().size(), 1.0);
            for (int i = 0; i < number_of_objectives; i++)
                b.add(ONE_VALUE);

            ArrayList<ArrayList<Data>> A = new ArrayList<>();
            for (Solution s : extreme_points) {
                ArrayList<Data> aux = new ArrayList<>();
                for (int i = 0; i < number_of_objectives; i++)
                    aux.add(s.getObjective(i));
                A.add(aux);
            }
            ArrayList<Data> x = guassianElimination(A, b);

            // Find intercepts
            for (int f = 0; f < number_of_objectives; f += 1) {
                intercepts.add(ONE_VALUE.div(x.get(f)));

            }
        }
        return intercepts;
    }

    public void normalizeObjectives(ArrayList<Solution> population, ArrayList<Data> intercepts,
            ArrayList<Data> ideal_point) {
        for (int t = 0; t < fronts.size(); t += 1) {
            for (Solution s : fronts.get(t)) {
                for (int f = 0; f < number_of_objectives; f++) {
                    ArrayList<Data> conv_obj = getAttribute(s);
                    if (intercepts.get(f).minus(ideal_point.get(f)).abs().compareTo(epsilon) > 0) {
                        // if (Math.abs(intercepts.get(f) - ideal_point.get(f)) > 10e-10) {
                        conv_obj.set(f, conv_obj.get(f).div(intercepts.get(f).minus(ideal_point.get(f))));
                    } else {
                        conv_obj.set(f, conv_obj.get(f).div(epsilon));
                    }

                }
            }
        }

    }

    public Data perpendicularDistance(List<Data> direction, ArrayList<Data> point) {
        Data numerator = ZERO_VALUE, denominator = ZERO_VALUE;
        for (int i = 0; i < direction.size(); i += 1) {
            numerator = numerator.plus(direction.get(i).times(point.get(i)));
            // numerator += direction.get(i) * point.get(i);
            denominator = denominator.plus(direction.get(i).pow(2));
            // denominator += Math.pow(direction.get(i), 2.0);
        }
        Data k = numerator.div(denominator);

        Data d = ZERO_VALUE;
        for (int i = 0; i < direction.size(); i += 1) {
            // d += Math.pow(k * direction.get(i) - point.get(i), 2.0);
            d = d.plus(k.times(direction.get(i).minus(point.get(i)).pow(2)));
        }
        return d.sqrt();
    }

    public void associate(ArrayList<Solution> population) throws CloneNotSupportedException {

        for (int t = 0; t < fronts.size(); t++) {
            for (Solution s : fronts.get(t)) {
                int min_rp = -1;
                Data min_dist = MAX_VALUE;
                Data d =ZERO_VALUE;
                for (int r = 0; r < this.referencePoints.getNumberOfPoints(); r++) {
                     d = perpendicularDistance(this.referencePoints.get(r).getPoint(),
                            (ArrayList<Data>) getAttribute(s));
                    if (d.compareTo(min_dist) < 0) {
                        min_dist = d;
                        min_rp = r;
                    }
                }
                if (t + 1 != fronts.size()) {
                    if (min_rp != -1)
                        this.referencePoints.get(min_rp).incrementPotentialMembers();
                } else {
                    // this.referencePoints.get(min_rp).AddPotentialMember(s, min_dist);
                    if(min_rp!=-1)
                    this.referencePoints.get(min_rp).addMember((Solution) s.clone(), min_dist);
                }
            }
        }

    }

    int FindNicheReferencePoint() {
        // find the minimal cluster size
        int min_size = Integer.MAX_VALUE;
        for (int i = 0; i < this.referencePoints.getNumberOfPoints(); i++)
            // if (referencePoints.get(i).getPotentialMembers()> 0)
            min_size = Math.min(min_size, referencePoints.get(i).getPotentialMembers());

        // find the reference points with the minimal cluster size Jmin
        ArrayList<Integer> min_rps = new ArrayList<>();

        for (int r = 0; r < this.referencePoints.getNumberOfPoints(); r += 1) {
            if (this.referencePoints.get(r).getPotentialMembers() == min_size) {
                min_rps.add(r);
            }
        }
        if (min_size == Integer.MAX_VALUE)
            return -1;
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
    Solution SelectClusterMember(int index) {
        Solution chosen = null;
        if (this.referencePoints.get(index).HasPotentialMember()) {
            if (this.referencePoints.get(index).getPotentialMembers() == 0) {
                chosen = this.referencePoints.get(index).FindClosestMember();
            } else {
                chosen = this.referencePoints.get(index).RandomMember();
            }
        }
        /*
         * if (rp.HasPotentialMember()) { if (rp.MemberSize() == 0) // currently has no
         * member { chosen = rp.FindClosestMember(); } else { chosen =
         * rp.RandomMember(); } }
         */
        return chosen;
    }

    @Override
    /*
     * This method performs the environmental Selection indicated in the paper
     * describing NSGAIII
     */
    public void execute(ArrayList<Solution> source) {
        // The comments show the C++ code

        // ---------- Steps 9-10 in Algorithm 1 ----------
        if (source.size() == this.pop_size) {
            parents = source;
            return;
        }

        // ---------- Step 14 / Algorithm 2 ----------
        // vector<double> ideal_point = TranslateObjectives(&cur, fronts);
        ArrayList<Data> ideal_point = translateObjectives(source);
        ArrayList<Solution> extreme_points = findExtremePoints(source);
        ArrayList<Data> intercepts = constructHyperplane(source, extreme_points);

        normalizeObjectives(source, intercepts, ideal_point);
        // ---------- Step 15 / Algorithm 3, Step 16 ----------
        try {
            associate(source);
        } catch (CloneNotSupportedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // ---------- Step 17 / Algorithm 4 ----------
        while (source.size() < this.pop_size) {
            int min_rp = FindNicheReferencePoint();
            if (min_rp != -1) {
                Solution chosen = SelectClusterMember(min_rp);
                if (chosen == null) // no potential member in Fl, disregard this reference point
                {
                    this.referencePoints.remove(min_rp);
                } else {
                    this.referencePoints.get(min_rp).incrementPotentialMembers();
                    this.referencePoints.get(min_rp).RemovePotentialMember(chosen);
                    // this.referencePoints.addMember(min_rp);
                    // this.referencePoints.RemovePotentialMember(min_rp, chosen);
                    source.add(chosen);
                }
            }
        }
        parents = source;

    }

    @Override
    public ArrayList<Solution> getParents() {
        return parents;
    }

    @Override
    public void setPopulaitonSize(int size) {
        // TODO Auto-generated method stub

    }

}