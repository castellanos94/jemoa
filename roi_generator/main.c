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
    while (count != k)
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
                int value;
                memcpy(sample[i]->variable, tmp->variable, numberOfVariables * sizeof(double));
                memcpy(sample[i]->objective, tmp->objective, numberOfObjectives * sizeof(double));
                destroy_solution(tmp);
            }
        }
        printf("... size of F0 %4d\n", count);
    }
    char fileNameRoi[128];
    snprintf(fileNameRoi, 128, "ROI_DTLZ%d_V%d_O%d.txt", problem, numberOfVariables, numberOfObjectives);

    printf("Saving ROI File..");

    FILE *f = fopen(fileNameRoi, "w");
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
    printf(". %s\n", fileNameRoi);
    fclose(f);
    int weakness[k];
    memset(weakness, 0, k);
    double net_score[k];
    memset(net_score, 0, k);

    printf("Looking best compromise ...\n");
    for (int i = 0; i < k; i++)
    {
        int step = k / 10.0;
        if (i != 0 && i % step == 0)
        {
            printf("Iteration (%3.2f) %6d of %6d ...\n", (1.0 * i / k), i, k);
        }
        double sigma_out = 0, sigma_in = 0;
        for (int j = 0; j < k; j++)
        {
            if (i != j)
            {
                struct Interval tmpi[instance.numberOfObjectives];
                struct Interval tmpj[instance.numberOfObjectives];
                for (int criterial = 0; criterial < instance.numberOfObjectives; criterial++)
                {
                    tmpi[criterial].lower = sample[i]->objective[criterial];
                    tmpi[criterial].upper = sample[i]->objective[criterial];
                    tmpj[criterial].lower = sample[j]->objective[criterial];
                    tmpj[criterial].upper = sample[j]->objective[criterial];
                }

                double *result = compare_by_preferences(instance, dm, tmpi, tmpj);
                //printf("i = %3d, j = %3d : r = %3d, xy = %f, yx =%f\n", i, j, result[0], result[1], result[2]);
                sigma_out += result[1];
                sigma_in += result[2];
                struct Interval sigin = {sigma_in, sigma_in};
                if (compareTo(instance.beta[dm], sigin) < 0 && sigma_out < 0.5)
                {
                    weakness[i]++;
                }

                free(result);
            }
        }
        net_score[i] = sigma_out - sigma_in;
    }
    double bestNetScore = net_score[0];
    int indexBestNetScore = 0;
    int indexWeakNess = -1;
    int candidatos_length = 0;
    int old_best[] = {-1, -1, -1, -1};
    int update_old = 3;
    for (int i = 0; i < k; i++)
    {
        if (net_score[i] > bestNetScore)
        {
            old_best[update_old--] = indexBestNetScore;
            if (update_old == -1)
            {
                update_old = 3;
            }
            bestNetScore = net_score[i];
            indexBestNetScore = i;
        }
        if (weakness[i] == 0)
        {
            candidatos_length++;
        }
        //printf("%3d : Weakness %3d, NetScore = %f\n", i, weakness[i], net_score[i]);
    }

    printf("Best NetScore %f\n", bestNetScore);
    struct Solution *bestCompromise;
    if (candidatos_length == 1)
    {
        for (int i = 0; i < k; i++)
        {
            if (weakness[i] == 0)
            {
                indexWeakNess = i;
                break;
            }
        }
    }
    else if (candidatos_length > 1)
    {
        for (int i = 0; i < k; i++)
        {
            if (weakness[i] == 1)
            {
                if (net_score[i] > bestNetScore)
                {
                    bestNetScore = net_score[i];
                    indexBestNetScore = i;
                }
            }
        }
    }
    int indexBest = -1;
    if (indexWeakNess != -1)
    {
        printf("Best compromise by weakness ...\n");
        bestCompromise = sample[indexWeakNess];
        indexBest = indexWeakNess;
    }
    else
    {

        bestCompromise = sample[indexBestNetScore];
        indexBest = indexBestNetScore;
        bestNetScore = net_score[indexBest];
        printf("Best compromise by netscore %f ...\n", bestNetScore);
    }
    printSolution(bestCompromise);
    snprintf(fileNameRoi, 128, "ROI_P_DTLZ%d_V%d_O%d.txt", problem, numberOfVariables, numberOfObjectives);
    printf("Saving ROI-P File... %s\n", fileNameRoi);

    FILE *roip = fopen(fileNameRoi, "w");
    if (roip == NULL)
    {
        printf("Error opening file!\n");
        exit(1);
    }
    // Export and destroy

    for (int i = 0; i < sample[indexBest]->numberOfVariables; i++)
    {
        if (i < sample[indexBest]->numberOfVariables - 1)
            fprintf(roip, "%18.16f, ", sample[indexBest]->variable[i]);
        else
            fprintf(roip, "%18.16f * ", sample[indexBest]->variable[i]);
    }
    for (int i = 0; i < sample[indexBest]->numberOfObjectives; i++)
    {
        if (i < sample[indexBest]->numberOfObjectives - 1)
            fprintf(roip, "%18.16f, ", sample[indexBest]->objective[i]);
        else
            fprintf(roip, "%18.16f * ", sample[indexBest]->objective[i]);
    }
    fprintf(roip, "%3d\n", sample[indexBest]->rank);
    // looking equal or best
    int roip_length = 0;
    for (int j = 0; j < k; j++)
    {
        int isValid = -1;
        if (indexWeakNess != -1)
        {
            if (weakness[j] == 0)
            {
                isValid = 1;
            }
        }
        else
        {
            if (net_score[j] >= bestNetScore && weakness == 0)
            {
                isValid = 1;
            }
        }
        if (isValid == 1 && indexBest != j)
        {
            roip_length++;
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
    }

    if (roip_length == 0)
    {
        sortNetScore(4, old_best, net_score);
        for (int l = 0; l < 4; l++)
        {
            for (int j = 0; j < k; j++)
            {
                if (old_best[l] == j && indexBest != j)
                {
                    printf("\tBest old netscore %f\n", net_score[j]);
                    roip_length++;
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
                    break;
                }
            }
        }
    }
    fclose(roip);
    printf("ROI preferential lenght %3d\n", roip_length);
    for (int i = 0; i < k; i++)
    {
        destroy_solution(sample[i]);
    }
    destroy_instance(instance);
    return 0;
}