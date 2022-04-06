#include <stdio.h>
#include <time.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>
#include "src/dominance.h"
#include "src/solution.h"
#include "src/dtlz.h"
#include "src/preferences.h"
#include "src/instance.h"
#define MAX_DOMINATION_INTENTS 50000

void printObjectives(int index, struct Solution *old, struct Solution *new)
{
    printf("%3d - ", index);
    for (int i = 0; i < old->numberOfObjectives; i++)
    {
        printf("%.5f ", old->objective[i]);
    }
    printf("<-> ");
    for (int i = 0; i < new->numberOfObjectives; i++)
    {
        printf("%.5f ", new->objective[i]);
    }
    printf("\n");
}
void sortNetScore(int size, int index[], double netScore[])
{
    for (int i = 0; i < size; i++)
    {
        for (int j = i + 1; j < size; j++)
        {
            if (index[i] != -1 && index[j] != -1 && netScore[index[j]] > netScore[index[i]])
            {
                int tmp = index[i];
                index[i] = index[j];
                index[j] = tmp;
            }
        }
    }
}

void solutionToFile(FILE *file, struct Solution *solution)
{
    for (int i = 0; i < solution->numberOfVariables; i++)
    {
        if (i < solution->numberOfVariables - 1)
            if (solution->variable[i] == 0.5 || solution->variable[i] == 0)
                fprintf(file, "%.1f, ", solution->variable[i]);
            else
                fprintf(file, "%.3f, ", solution->variable[i]);
        else
        {
            if (solution->variable[i] == 0.5 || solution->variable[i] == 0)
                fprintf(file, "%.1f * ", solution->variable[i]);
            else
                fprintf(file, "%.3f * ", solution->variable[i]);
        }
    }
    for (int i = 0; i < solution->numberOfObjectives; i++)
    {
        if (i < solution->numberOfObjectives - 1)
            fprintf(file, "%.5f, ", solution->objective[i]);
        else
            fprintf(file, "%.5f * ", solution->objective[i]);
    }
    fprintf(file, "%3d\n", solution->rank);
}

void objectiveTofile(FILE *file, struct Solution *solution)
{
    for (int i = 0; i < solution->numberOfObjectives; i++)
    {
        if (i < solution->numberOfObjectives - 1)
            fprintf(file, "%.5f, ", solution->objective[i]);
        else
            fprintf(file, "%.5f\n", solution->objective[i]);
    }

}


void printInstanceWithCompromise(char *fileName, struct Instance instance, struct Solution *solutions[], int indexBestCompromise, int indexCandidate[], int sizeCandidate)
{
    //printf("NumberOfVariables %3d, NumberOfObjectives %3d, DMs %3d\n", instance.numberOfVariables, instance.numberOfObjectives, instance.numberOfDM);
    FILE *file = fopen(fileName, "w");
    if (file == NULL)
    {
        printf("Error opening file!\n");
        exit(1);
    }
    fprintf(file, "%d //Objectives\n%d //Vars\n%d //DMS\n", instance.numberOfObjectives, instance.numberOfVariables, instance.numberOfDM);
    // printf("Weigth\n");
    for (int j = 0; j < instance.numberOfDM; j++)
    {
        // printf("\t");
        for (int i = 0; i < instance.numberOfObjectives; i++)
        {
            if (i < instance.numberOfObjectives - 1)
                fprintf(file, "%s, ", toString(instance.weight[j][i]));
            else
                fprintf(file, "%s // Weight\n", toString(instance.weight[j][i]));
        }
    }

    //printf("Veto\n");
    for (int j = 0; j < instance.numberOfDM; j++)
    {
        // printf("\t");
        for (int i = 0; i < instance.numberOfObjectives; i++)
        {
            if (i < instance.numberOfObjectives - 1)
                fprintf(file, "%s, ", toString(instance.veto[j][i]));
            else
                fprintf(file, "%s //Veto\n", toString(instance.veto[j][i]));
        }
    }

    //printf("Beta\n");
    for (int i = 0; i < instance.numberOfDM; i++)
    {
        fprintf(file, "%s //Beta %d\n", toString(instance.beta[i]), i);
    }
    // printf("Lamda\n");
    for (int i = 0; i < instance.numberOfDM; i++)
    {
        fprintf(file, "%s //Lambda %d\n", toString(instance.lambda[i]), i);
    }
    /* printf("Alpha\n");
    for (int i = 0; i < instance.numberOfDM; i++)
    {
        printf("%f\n", instance.alpha[i]);
    }*/
    fprintf(file, "TRUE TRUE //Best compromise \n1\n");
    objectiveTofile(file, solutions[indexBestCompromise]);
    fprintf(file, "TRUE FALSE //Frontiers \n2\n");
    int startIndex = (sizeCandidate/2.0 > 0) ? sizeCandidate/2 : 0;
    printf("from %d to", startIndex);
    int count = 0;
    while(startIndex<sizeCandidate && count < 2)
    {
        for (int j = 0; j < instance.numberOfObjectives; j++)
        {
            if (j < instance.numberOfObjectives - 1)
            {
                fprintf(file, "%.5f, ", solutions[indexCandidate[startIndex]]->objective[j]);
            }
            else
            {
                fprintf(file, "%.5f\n", solutions[indexCandidate[startIndex]]->objective[j]);
            }
        }
        startIndex = sizeCandidate - 1;
        count++;
    }
    printf(" %d\n", startIndex);
    fprintf(file, "FALSE //Initial solutions");
    fclose(file);
}
int main(int argc, char const *argv[])
{
    if (argc == 1)
    {
        printf("Error arguments not passed, required :\n");
        printf("\tint : sample size\nOptional:\n\tlong : seed\n");
        exit(128);
    }
    const int problem = atoi(argv[1]);
    int sample_size = atoi(argv[2]);
    int seed = -1;
    if (argc == 5)
    {

        seed = atol(argv[4]);
        srand(seed);
    }
    else
    {
        srand(time(NULL));
    }

    const int k = sample_size;
    printf("Command: Problem %2d, sample %d, seed %d\n", problem, k, seed);

    struct Instance instance = readInstance(argv[3]);

    const int dm = 0;
    const int numberOfObjectives = instance.numberOfObjectives;
    const int numberOfVariables = instance.numberOfVariables;
    printInstance(instance);
    printf("Sample size %5d\n", k);

    struct Solution *sample[k]; //= (Solution*)malloc(k*sizeof(Solution));
    printf("Generating sample ...\n");
    for (int i = 0; i < k; i++)
    {
        sample[i] = generateAnalyticalSolution(problem, numberOfVariables, numberOfObjectives);
    }

    int count = 0;
    int intents = 0;
    int dominate_me[k];
    while (count != k && intents < MAX_DOMINATION_INTENTS)
    {
        printf("\tCheck dominance %3d... ", intents++);

        for (int i = 0; i < k; i++)
        {
            dominate_me[i] = 0;
        }

        for (int i = 0; i < k; i++)
        {
            for (int j = 0; j < k; j++)
            {
                int value = dominance(numberOfObjectives, sample[i]->objective, sample[j]->objective);                
                if (value == -1)
                {
                    dominate_me[j]++;
                }
                else if (value == 1)
                {
                    dominate_me[i]++;
                }
            }
        }
        count = 0;
        for (int i = 0; i < k; i++)
        {
            if (dominate_me[i] == 0)
            {
                //printSolution(sample[i]);
                count++;
            }
            else
            {
                struct Solution *tmp = generateAnalyticalSolution(problem, numberOfVariables, numberOfObjectives);             
                
                memcpy(sample[i]->variable, tmp->variable, numberOfVariables * sizeof(double));
                memcpy(sample[i]->objective, tmp->objective, numberOfObjectives * sizeof(double));
                destroy_solution(tmp);
            }
        }
        printf("... size of F0 %4d\n", count);
    }    
    
    char fileNameRoi[128];
    snprintf(fileNameRoi, 128, "ROI_P_DTLZ%d_V%d_O%d_dom.txt", problem, numberOfVariables, numberOfObjectives);
    printf("Saving ROI-P File... %s\n", fileNameRoi);

    FILE *roip = fopen(fileNameRoi, "w");
    if (roip == NULL)
    {
        printf("Error opening file!\n");
        exit(1);
    }
    int index[100];
    for (int j = 0; j < k; j++)
    {
        solutionToFile(roip, sample[j]);
        if(j < 100)
        index[j] = j;
    }
    fclose(roip);
    printf("ROI lenght %3d\n", k);
    printf("Making instance\n");
    snprintf(fileNameRoi, 128, "DTLZ%d_Instance_dom.txt", problem);
    printInstanceWithCompromise(fileNameRoi, instance, sample, 0, index, 100);
    for (int i = 0; i < k; i++)
    {
        destroy_solution(sample[i]);
    }
    destroy_instance(instance);
    return 0;
}
