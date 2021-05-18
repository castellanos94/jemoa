package com.castellanos94.problems;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.castellanos94.datatype.Data;
import com.castellanos94.datatype.IntegerData;
import com.castellanos94.datatype.Interval;
import com.castellanos94.datatype.RealData;
import com.castellanos94.instances.PSPI_Instance;
import com.castellanos94.preferences.impl.OutrankingModel;
import com.castellanos94.solutions.BinarySolution;
import com.castellanos94.utils.Tools;

/**
 * Portafolio Social Problem for Group Decision
 */
public class PSPI_GD extends GDProblem<BinarySolution> {
    protected OutrankingModel[] preference_models;
    protected int dms;
    private List<Integer> positions;

    public PSPI_GD(PSPI_Instance instance_GD) {
        this.instance = instance_GD;
        this.numberOfObjectives = instance_GD.getNumObjectives();
        this.numberOfConstrains = 1;
        this.numberOfDecisionVars = instance_GD.getNumProjects();
        this.dms = instance_GD.getNumDMs();
        this.lowerBound = new Data[this.numberOfDecisionVars];
        this.upperBound = new Data[this.numberOfDecisionVars];
        this.objectives_type = new int[this.numberOfObjectives];
        this.preference_models = instance_GD.getOutrankingModels();
        for (int i = 0; i < this.numberOfObjectives; i++) {
            objectives_type[i] = Problem.MAXIMIZATION;
        }
        for (int i = 0; i < lowerBound.length; i++) {
            lowerBound[i] = IntegerData.ZERO;
            upperBound[i] = IntegerData.ONE;
        }
        positions = IntStream.range(0, numberOfDecisionVars).boxed().collect(Collectors.toList());
        // makePreferences();
    }

    /*
     * private void makePreferences() { preferenceModels = new PreferenceModel[dms];
     * for (int i = 0; i < dms; i++) { preferenceModels[i] = new PreferenceModel();
     * preferenceModels[i].setAlpha(getInstance().getAlphas_DMs()[i]);
     * preferenceModels[i].setBeta(getInstance().getBetas_DMs()[i]);
     * preferenceModels[i].setLambda(getInstance().getLambdas_DMs()[i]);
     * preferenceModels[i].setVetos(getInstance().getVetos_DMs()[i]);
     * preferenceModels[i].setWeights(getInstance().getWeights_DMs()[i]); } }
     */

    @Override
    public void evaluate(BinarySolution solution) {
        Interval currentBudget = Interval.ZERO;
        Interval[][] projects = getInstance().getProjects();
        ArrayList<Data> objectives = new ArrayList<>();
        for (int i = 0; i < this.numberOfObjectives; i++) {
            objectives.add(new Interval(0));
        }
        for (int i = 0; i < this.numberOfDecisionVars; i++) {
            if (solution.getVariable(0).get(i)) {
                currentBudget = (Interval) currentBudget.plus(projects[i][0]);
                for (int j = 0; j < this.numberOfObjectives; j++) {
                    objectives.set(j, objectives.get(j).plus(projects[i][j + 1]));
                }
            }
        }
        solution.setResource(0, currentBudget);
        solution.setObjectives(objectives);

    }

    /**
     * No se encontraron diference entre los modelos UF y Outranking que definiera
     * crear otra clase.
     */
    @Override
    public void evaluateConstraint(BinarySolution solution) {
        Interval suma = new Interval(solution.getResources().get(0));
        RealData poss = this.getInstance().getBudget().possGreaterThanOrEq(suma);
        if (poss.compareTo(0.72) < 0) {
            System.out.println(poss + " " + 0.72);
            Data tmp = getInstance().getBudget().minus(solution.getResources().get(0));
            solution.setPenalties(tmp);
            solution.setNumberOfPenalties(1);
        } else {
            solution.setPenalties(RealData.ZERO);
            solution.setNumberOfPenalties(0);
        }
    }

    public BinarySolution createFromString(Interval[] data) {
        BinarySolution sol = new BinarySolution(this);
        for (int i = 0; i < data.length; i++) {
            sol.getVariable(0).set(i, data[i].compareTo(1) == 0);
        }
        return sol;
    }

    @Override
    public BinarySolution randomSolution() {
        BinarySolution sol = new BinarySolution(this);

        Tools.shuffle(positions);
        Interval[][] projects = getInstance().getProjects();
        Interval budget = getInstance().getBudget();
        Interval current_budget = new Interval(0);
        for (int i = 0; i < positions.size(); i++) {
            Interval suma = (Interval) projects[positions.get(i)][0].plus(current_budget);
            RealData possGreaterThanOrEq = budget.possGreaterThanOrEq(suma);
            if (possGreaterThanOrEq.compareTo(0.72) >= 0) {
                // sol.setVariables(positions.get(i), new IntegerData(1));
                sol.getVariable(0).set(positions.get(i));
                current_budget = suma;
            }
        }
        sol.setResource(0, current_budget);
        return sol;
    }

    public OutrankingModel getPreferenceModel(int dm) {
        return this.preference_models[dm];
    }

    public OutrankingModel[] getPreferenceModels() {
        return preference_models;
    }

    @Override
    public PSPI_Instance getInstance() {
        return (PSPI_Instance) this.instance;
    }

    @Override
    public Interval[][][] getR2() {
        return getInstance().getR2();
    }

    @Override
    public Interval[][][] getR1() {
        return getInstance().getR1();
    }

    @Override
    public int getNumDMs() {
        return getInstance().getNumDMs();
    }

    @Override
    public BinarySolution getEmptySolution() {
        return new BinarySolution(this);
    }
}