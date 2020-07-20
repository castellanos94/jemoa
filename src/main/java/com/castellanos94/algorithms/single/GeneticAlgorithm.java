package com.castellanos94.algorithms.single;

import java.util.ArrayList;
import java.util.Collection;

import com.castellanos94.algorithms.AbstractEvolutionaryAlgorithm;
import com.castellanos94.problems.Problem;
import com.castellanos94.solutions.Solution;

public class GeneticAlgorithm extends AbstractEvolutionaryAlgorithm {

    public GeneticAlgorithm(Problem problem) {
        super(problem);
    }

    @Override
    protected ArrayList<Solution> reproduction(ArrayList<Solution> parents) {
        ArrayList<Solution> offspring = new ArrayList<>();
        for (int i = 0; i < parents.size(); i++) {
            ArrayList<Solution> p = new ArrayList<>();
            p.add(parents.get(i++));
            p.add(parents.get(i));
            offspring.addAll(crossoverOperator.execute(p));

        }
        for (Solution solution : offspring) {
            mutationOperator.execute(solution);
        }
        return offspring;
    }

    @Override
    protected ArrayList<Solution> replacement(ArrayList<Solution> population, ArrayList<Solution> offspring) {
        ArrayList<Solution> newPop = new ArrayList<>();
        newPop.addAll((Collection<? extends Solution>) population.clone());
        for (int i = 0; i < offspring.size(); i++) {
            Solution a = offspring.get(i);
            for (int j = 0; j < newPop.size(); j++) {
                int value = a.getObjectives().get(0).compareTo(newPop.get(j).getObjectives().get(0));
                int penal = a.getN_penalties().compareTo(newPop.get(j).getN_penalties());
                if (Problem.MAXIMIZATION == problem.getObjectives_type()[0]) {
                    if (value == 1 && penal < 0) {
                        try {
                            newPop.set(j, (Solution) a.clone());
                        } catch (CloneNotSupportedException e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                } else {
                    if (value == -1 && penal < 0) {
                        try {
                            newPop.set(j, (Solution) a.clone());
                        } catch (CloneNotSupportedException e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                }
            }
        }
        return newPop;
    }

}