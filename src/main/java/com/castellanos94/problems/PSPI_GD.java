package com.castellanos94.problems;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.castellanos94.datatype.Data;
import com.castellanos94.datatype.IntegerData;
import com.castellanos94.datatype.Interval;
import com.castellanos94.instances.PspIntervalInstance_GD;
import com.castellanos94.preferences.impl.PreferenceModel;
import com.castellanos94.solutions.Solution;
import com.castellanos94.utils.Tools;

/**
 * Portafolio Social Problem for Group Decision
 */
public class PSPI_GD extends Problem {
    protected PspIntervalInstance_GD pspInstance;
    protected PreferenceModel[] preferenceModels;
    protected int dms;
    private List<Integer> positions;

    public PSPI_GD(PspIntervalInstance_GD instance_GD) {
        this.instance = instance_GD;
        this.pspInstance = instance_GD;
        this.numberOfObjectives = pspInstance.getNumObjectives();
        this.numberOfConstrains = 1;
        this.numberOfDecisionVars = pspInstance.getNumProjects();
        this.dms = this.pspInstance.getNumDMs();
        this.lowerBound = new Data[this.numberOfDecisionVars];
        this.upperBound = new Data[this.numberOfDecisionVars];
        this.objectives_type = new int[this.numberOfObjectives];
        for (int i = 0; i < this.numberOfObjectives; i++) {
            objectives_type[i] = Problem.MAXIMIZATION;
        }
        for (int i = 0; i < lowerBound.length; i++) {
            lowerBound[i] = IntegerData.ZERO;
            upperBound[i] = IntegerData.ONE;
        }
        positions = IntStream.range(0, numberOfDecisionVars).boxed().collect(Collectors.toList());
        makePreferences();
    }

    private void makePreferences() {
        preferenceModels = new PreferenceModel[dms];
        for (int i = 0; i < dms; i++) {
            preferenceModels[i] = new PreferenceModel();
            preferenceModels[i].setAlpha(pspInstance.getAlphas_DMs()[i]);
            preferenceModels[i].setBeta(pspInstance.getBetas_DMs()[i]);
            preferenceModels[i].setLambda(pspInstance.getLambdas_DMs()[i]);
            preferenceModels[i].setVetos(pspInstance.getVetos_DMs()[i]);
            preferenceModels[i].setWeights(pspInstance.getWeights_DMs()[i]);
        }
    }

    @Override
    public void evaluate(Solution solution) {
        Interval currentBudget = Interval.ZERO;
        Interval[][] projects = pspInstance.getProjects();
        ArrayList<Data> objectives = new ArrayList<>();
        for (int i = 0; i < this.numberOfObjectives; i++) {
            objectives.add(new Interval(0));
        }
        for (int i = 0; i < this.numberOfDecisionVars; i++) {
            if (solution.getVariable(i).compareTo(1) == 0) {
                currentBudget = (Interval) currentBudget.plus(projects[i][0]);
                for (int j = 0; j < this.numberOfObjectives; j++) {
                    objectives.set(j, objectives.get(j).plus(projects[i][j + 1]));
                }
            }
        }
        solution.setResource(0, currentBudget);
        solution.setObjectives(objectives);

    }

    @Override
    public int evaluateConstraints(Solution solution) {
        if (solution.getResources().get(0).compareTo(this.pspInstance.getBudget()) > 0) {
            Data tmp = pspInstance.getBudget().minus(solution.getResources().get(0));
            solution.setPenalties(tmp);
            solution.setN_penalties(1);
        } else {
            solution.setPenalties(Interval.ZERO);
            solution.setN_penalties(0);
        }
        return 0;
    }

    @Override
    public Solution randomSolution() {
        Solution sol = new Solution(this);

        Tools.shuffle(positions);
        Interval[][] projects = pspInstance.getProjects();
        Interval budget = pspInstance.getBudget();
        Interval current_budget = new Interval(0);
        for (int i = 0; i < positions.size(); i++) {
            if (projects[positions.get(i)][0].plus(current_budget).compareTo(budget) <= 0) {
                sol.setDecisionVar(positions.get(i), new IntegerData(1));
                current_budget = (Interval) current_budget.plus(projects[positions.get(i)][0]);
            } else {
                sol.setDecisionVar(positions.get(i), new IntegerData(0));
            }
        }
        sol.setResource(0, current_budget);
        return sol;
    }

    public PreferenceModel getPreferenceModel(int dm) {
        return this.preferenceModels[dm];
    }

    public PreferenceModel[] getPreferenceModels() {
        return preferenceModels;
    }


}