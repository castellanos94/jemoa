#include <stdio.h>
#include "solution.h"
#include <stdlib.h>
#include <string.h>

struct Solution init_solution(int numberOfVariables, int numberOfObjectives)
{
    /*Solution solution = malloc(sizeof(Solution));
    if (solution == NULL)
        return NULL;*/
    struct Solution solution = {.numberOfObjectives = numberOfObjectives,
                                .numberOfVariables = numberOfVariables,
                                .variable = (double *)malloc(sizeof(double) * numberOfVariables),
                                .objective = (double *)malloc(sizeof(double) * numberOfObjectives)};

    //memcpy(solution, &init, sizeof *solution);
    //solution.objective =
    //solution.variable = (double *)malloc(sizeof(double) * numberOfVariables);
    return solution;
}

void destroy_solution(struct Solution *solution)
{
    if (solution == NULL)
        return;
    if (solution->objective != NULL)
    {
        free(solution->objective);
    }
    if (solution->variable != NULL)
    {
        free(solution->variable);
    }
    free(solution);
}

void printSolution(struct Solution *solutionToPrint)
{
    for (int i = 0; i < solutionToPrint->numberOfVariables; i++)
    {
        if (i < solutionToPrint->numberOfVariables - 1)
            printf("%f, ", solutionToPrint->variable[i]);
        else
            printf("%f * ", solutionToPrint->variable[i]);
    }
    for (int i = 0; i < solutionToPrint->numberOfObjectives; i++)
    {
        if (i < solutionToPrint->numberOfObjectives - 1)
            printf("%f, ", solutionToPrint->objective[i]);
        else
            printf("%f * ", solutionToPrint->objective[i]);
    }
    printf("%3d\n", solutionToPrint->rank);
}
