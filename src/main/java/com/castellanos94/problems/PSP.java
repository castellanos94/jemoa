package com.castellanos94.problems;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.castellanos94.datatype.Data;
import com.castellanos94.datatype.IntegerData;
import com.castellanos94.instances.PSPInstance;
import com.castellanos94.solutions.BinarySolution;
import com.castellanos94.utils.Tools;

/*
*@author Castellanos Alvarez, Alejandro
*@since 22/03/2020
*/
public class PSP extends Problem<BinarySolution> {

    public PSP(PSPInstance instance) {
        this.instance = instance;
        this.numberOfObjectives = instance.getData("nObj").intValue();
        this.numberOfDecisionVars = instance.getData("nProjects").intValue();
        this.objectives_type = new int[numberOfObjectives];
        for (int i = 0; i < objectives_type.length; i++) {
            objectives_type[i] = Problem.MAXIMIZATION;
        }
        this.numberOfConstrains = 1;
    }

    @Override
    public void evaluate(BinarySolution solution) {

        Data[] objs = new Data[numberOfObjectives];
        for (int i = 0; i < objs.length; i++) {
            objs[i] = new IntegerData(0);
        }
        Data[][] projects = instance.getDataMatrix("projects");
        Data current_budget = new IntegerData(0);

        for (int i = 0; i < this.numberOfDecisionVars; i++) {
            if (solution.getVariable(0).get(i)) {
                current_budget = current_budget.plus(projects[i][0]);
                for (int j = 0; j < numberOfObjectives; j++) {
                    objs[j] = objs[j].plus(projects[i][3 + j]);
                }
            }
        }
        solution.setResource(0, current_budget);
        solution.setObjectives(new ArrayList<>(Arrays.asList(objs)));
    }

    @Override
    public void evaluateConstraint(BinarySolution sol) {
        Data budget = instance.getData("budget");
        IntegerData current_budget = new IntegerData(0);
        Data[][] projects = instance.getDataMatrix("projects");

        IntegerData[][] areas = (IntegerData[][]) instance.getDataMatrix("areas");
        IntegerData[][] regions = (IntegerData[][]) instance.getDataMatrix("regions");
        Data areaSum[] = new Data[areas.length];
        for (int i = 0; i < areaSum.length; i++) {
            areaSum[i] = new IntegerData(0);
        }
        Data regionSum[] = new Data[regions.length];
        for (int i = 0; i < regionSum.length; i++) {
            regionSum[i] = new IntegerData(0);
        }

        for (int i = 0; i < numberOfDecisionVars; i++) {
            if (sol.getVariable(0).get(i)) {
                // if (sol.getVariables().get(i).compareTo(one) == 0) {
                current_budget = (IntegerData) current_budget.plus(projects[i][0]);
                int area = projects[i][1].intValue() - 1;
                int region = projects[i][2].intValue() - 1;
                areaSum[area] = areaSum[area].plus(projects[i][0]);
                regionSum[region] = regionSum[region].plus(projects[i][0]);
            }
        }
        Data penaltie = new IntegerData(0);
        int penalties = 0;

        if (current_budget.compareTo(budget) > 0) {
            penalties++;
            penaltie = budget.minus(current_budget);
        }
        for (int i = 0; i < regionSum.length; i++) {
            if (regionSum[i].compareTo(regions[i][0]) < 0) {// limite inferior
                penaltie = penaltie.plus(regionSum[i].minus(regions[i][0]));
                penalties++;
            }
            if (regionSum[i].compareTo(regions[i][1]) > 0) { // limite superior
                penaltie = penaltie.plus(regions[i][1].minus(regionSum[i]));
                penalties++;
            }
        }
        for (int i = 0; i < areaSum.length; i++) {
            if (areaSum[i].compareTo(areas[i][0]) < 0) {
                penaltie = penaltie.plus(areaSum[i].minus(areas[i][0]));
                penalties++;
            }
            if (areaSum[i].compareTo(areas[i][1]) > 0) {
                penaltie = penaltie.plus(areas[i][1].minus(areaSum[i]));
                penalties++;
            }
        }

        sol.setNumberOfPenalties(penalties);
        sol.setPenalties(penaltie);
    }

    @Override
    public BinarySolution randomSolution() {
        BinarySolution sol = new BinarySolution(this);
        List<Integer> positions = IntStream.range(0, numberOfDecisionVars).boxed().collect(Collectors.toList());
        Collections.shuffle(positions);
        Data[][] projects = instance.getDataMatrix("projects");
        Data budget = instance.getData("budget");
        IntegerData current_budget = new IntegerData(0);
        for (int i = 0; i < positions.size(); i++) {
            if (Tools.getRandom().nextDouble() < 0.5
                    && projects[positions.get(i)][0].plus(current_budget).compareTo(budget) <= 0) {
                sol.getVariable(0).set(positions.get(i));
                current_budget = (IntegerData) current_budget.plus(projects[positions.get(i)][0]);
            }
        }
        sol.setResource(0, current_budget);
        return sol;
    }

    @Override
    public BinarySolution getEmptySolution() {
        return new BinarySolution(this);
    }

}