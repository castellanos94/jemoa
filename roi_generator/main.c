#include <stdio.h>
#include <time.h>
#include <stdlib.h>
#include <string.h>
#include "dominance.h"
#include "solution.h"
#include "dtlz.h"

void printObjectives(int index, struct Solution *old, struct Solution *new)
{
    printf("%3d - ", index);
    for (int i = 0; i < old->numberOfObjectives; i++)
    {
        printf("%5f ", old->objective[i]);
    }
    printf("<-> ");
    for (int i = 0; i < new->numberOfObjectives; i++)
    {
        printf("%5f ", new->objective[i]);
    }
    printf("\n");
}

int main(int argc, char const *argv[])
{

    int sample_size = atoi(argv[1]);
    int seed = -1;
    if (argc == 3)
    {

        seed = atol(argv[2]);
        srand(seed);
    }
    else
    {
        srand(time(NULL));
    }

    const int k = sample_size * 1000;
    printf("Command: sample %d, seed %d\n", k, seed);
    const int problem = 1;
    const int numberOfObjectives = 3;
    const int numberOfVariables = 7;
    printf("Problem %2d, number of variables %3d, number of objectives %3d\n", problem, numberOfVariables, numberOfObjectives);
    printf("Sample size %5d\n", k);
    /* struct Solution *sample = generateAnalyticalSolution(problem, numberOfVariables, numberOfObjectives);
    printSolution(sample);
    destroy_solution(sample);
    sample = generateAnalyticalSolution(problem, numberOfVariables, numberOfObjectives);
    printSolution(sample);
    destroy_solution(sample);
    //printSolution(sample);*/

    struct Solution *sample[k]; //= (Solution*)malloc(k*sizeof(Solution));
    printf("Generating sample ...");
    for (int i = 0; i < k; i++)
    {
        sample[i] = generateAnalyticalSolution(problem, numberOfVariables, numberOfObjectives);
        //memcpy(sample[i],generateAnalyticalSolution(problem, numberOfVariables, numberOfObjectives),)
    }

    int count = 0;
    int intents = 0;
    int dominate_me[k];
    while (count != k && intents < 50)
    {
        printf("Check dominance %3d... ", intents++);

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
        int replaced = 0;
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
                int value;              
                memcpy(sample[i]->variable, tmp->variable, numberOfVariables * sizeof(double));
                memcpy(sample[i]->objective, tmp->objective, numberOfObjectives * sizeof(double));
                destroy_solution(tmp);
                replaced++;
            }
        }
        printf("Size of F0 %4d, replaced %3d\n", count, replaced);
    }
    FILE *f = fopen("roi.txt", "w");
    if (f == NULL)
    {
        printf("Error opening file!\n");
        exit(1);
    }
    // Export and destroy
    for (int j = 0; j < k; j++)
    {
        for (int i = 0; i < sample[j]->numberOfVariables; i++)
        {
            if (i < sample[j]->numberOfVariables - 1)
                fprintf(f, "%18.16f, ", sample[j]->variable[i]);
            else
                fprintf(f, "%18.16f * ", sample[j]->variable[i]);
        }
        for (int i = 0; i < sample[j]->numberOfObjectives; i++)
        {
            if (i < sample[j]->numberOfObjectives - 1)
                fprintf(f, "%18.16f, ", sample[j]->objective[i]);
            else
                fprintf(f, "%18.16f * ", sample[j]->objective[i]);
        }
        fprintf(f, "%3d\n", sample[j]->rank);
    }

    fclose(f);
    for (int i = 0; i < k; i++)
    {
        destroy_solution(sample[i]);
    }
    return 0;
}