#include <stdio.h>
#include <string.h>
#include "instance.h"
#define MAX_LINE_LENGTH 6 * 1024

char *ltrim(char *str, const char *seps)
{
    size_t totrim;
    if (seps == NULL)
    {
        seps = "\t\n\v\f\r ";
    }
    totrim = strspn(str, seps);
    if (totrim > 0)
    {
        size_t len = strlen(str);
        if (totrim == len)
        {
            str[0] = '\0';
        }
        else
        {
            memmove(str, str + totrim, len + 1 - totrim);
        }
    }
    return str;
}

char *rtrim(char *str, const char *seps)
{
    int i;
    if (seps == NULL)
    {
        seps = "\t\n\v\f\r ";
    }
    i = strlen(str) - 1;
    while (i >= 0 && strchr(seps, str[i]) != NULL)
    {
        str[i] = '\0';
        i--;
    }
    return str;
}

char *trim(char *str, const char *seps)
{
    return ltrim(rtrim(str, seps), seps);
}

void printInstance(struct Instance instance)
{
    printf("NumberOfVariables %3d, NumberOfObjectives %3d, DMs %3d\n", instance.numberOfVariables, instance.numberOfObjectives, instance.numberOfDM);
    printf("Weigth\n");
    for (int j = 0; j < instance.numberOfDM; j++)
    {
        printf("\t");
        for (int i = 0; i < instance.numberOfObjectives; i++)
        {
            if (i < instance.numberOfObjectives - 1)
                printf("%s, ", toString(instance.weight[j][i]));
            else
                printf("%s\n", toString(instance.weight[j][i]));
        }
    }

    printf("Veto\n");
    for (int j = 0; j < instance.numberOfDM; j++)
    {
        printf("\t");
        for (int i = 0; i < instance.numberOfObjectives; i++)
        {
            if (i < instance.numberOfObjectives - 1)
                printf("%s, ", toString(instance.veto[j][i]));
            else
                printf("%s\n", toString(instance.veto[j][i]));
        }
    }

    printf("Beta\n");
    for (int i = 0; i < instance.numberOfDM; i++)
    {
        printf("\t%s\n", toString(instance.beta[i]));
    }
    printf("Lamda\n");
    for (int i = 0; i < instance.numberOfDM; i++)
    {
        printf("\t%s\n", toString(instance.lambda[i]));
    }
    printf("Alpha\n");
    for (int i = 0; i < instance.numberOfDM; i++)
    {
        printf("\t%f\n", instance.alpha[i]);
    }
}
struct Instance readInstance(const char *path)
{
    /* Open file */
    FILE *file = fopen(path, "r");

    if (!file)
    {
        perror(path);
        exit(EXIT_FAILURE);
    }
    printf("Reading instance\n");

    char buffer[MAX_LINE_LENGTH];
    //read number of objectives
    char *token = strtok(fgets(buffer, MAX_LINE_LENGTH, file), " ");
    int numberOfObjectives = atoi(token);
    token = strtok(fgets(buffer, MAX_LINE_LENGTH, file), " ");
    int numberOfVariables = atoi(token);
    token = strtok(fgets(buffer, MAX_LINE_LENGTH, file), " ");
    int numberOfDM = atoi(token);
    //Read weight for dms
    struct Interval weight[numberOfDM][numberOfObjectives];
    for (int i = 0; i < numberOfDM; i++)
    {
        fgets(buffer, MAX_LINE_LENGTH, file);
        token = strtok(buffer, ",");
        int j = 0;
        while (token != NULL)
        {

            sscanf(trim(token, NULL), "%lf %lf", &weight[i][j].lower, &weight[i][j].upper);
            token = strtok(NULL, ",");
            j++;
        }
    }
    //Read veto for dms
    struct Interval veto[numberOfDM][numberOfObjectives];
    for (int i = 0; i < numberOfDM; i++)
    {
        fgets(buffer, MAX_LINE_LENGTH, file);
        token = strtok(buffer, ",");
        int j = 0;
        while (token != NULL)
        {

            sscanf(trim(token, NULL), "%lf %lf", &veto[i][j].lower, &veto[i][j].upper);
            token = strtok(NULL, ",");
            j++;
        }
    }
    //Read beta for dms
    struct Interval beta[numberOfDM];
    for (int i = 0; i < numberOfDM; i++)
    {
        fgets(buffer, MAX_LINE_LENGTH, file);
        token = strtok(buffer, ",");
        while (token != NULL)
        {

            sscanf(trim(token, NULL), "%lf %lf", &beta[i].lower, &beta[i].upper);
            token = strtok(NULL, ",");
        }
    }
    struct Interval lambda[numberOfDM];
    for (int i = 0; i < numberOfDM; i++)
    {
        fgets(buffer, MAX_LINE_LENGTH, file);
        token = strtok(buffer, ",");
        while (token != NULL)
        {

            sscanf(trim(token, NULL), "%lf %lf", &lambda[i].lower, &lambda[i].upper);
            token = strtok(NULL, ",");
        }
    }

    //printf("Number of objectives %d, number of variables %d, number of dm %d\n", numberOfObjectives, numberOfVariables, numberOfDM);
    /* Close file */
    if (fclose(file))
    {
        perror(path);
        exit(EXIT_FAILURE);
    }

    struct Instance instance; //= {.numberOfObjectives = 3, .numberOfVariables = 7, .numberfOfDMS = 1};
    instance.numberOfDM = numberOfDM;
    instance.numberOfObjectives = numberOfObjectives;
    instance.numberOfVariables = numberOfVariables;
    instance.weight = (struct Interval **)malloc(sizeof(struct Interval) * numberOfDM);
    for (int i = 0; i < numberOfDM; i++)
    {
        instance.weight[i] = (struct Interval *)malloc(sizeof(struct Interval) * numberOfObjectives);
        for (int j = 0; j < numberOfObjectives; j++)
        {
            instance.weight[i][j].lower = weight[i][j].lower;
            instance.weight[i][j].upper = weight[i][j].upper;
        }
    }
    instance.veto = (struct Interval **)malloc(sizeof(struct Interval) * numberOfDM);
    for (int i = 0; i < numberOfDM; i++)
    {
        instance.veto[i] = (struct Interval *)malloc(sizeof(struct Interval) * numberOfObjectives);
        for (int j = 0; j < numberOfObjectives; j++)
        {
            instance.veto[i][j].lower = veto[i][j].lower;
            instance.veto[i][j].upper = veto[i][j].upper;
        }
    }
    instance.beta = (struct Interval *)malloc(sizeof(struct Interval) * numberOfDM);
    for (int i = 0; i < numberOfDM; i++)
    {
        instance.beta[i].lower = beta[i].lower;
        instance.beta[i].upper = beta[i].upper;
    }

    instance.lambda = (struct Interval *)malloc(sizeof(struct Interval) * numberOfDM);
    for (int i = 0; i < numberOfDM; i++)
    {
        instance.lambda[i].lower = lambda[i].lower;
        instance.lambda[i].upper = lambda[i].upper;
    }
    instance.alpha = (double *)malloc(sizeof(double) * numberOfDM);
    for (int i = 0; i < numberOfDM; i++)
    {
        instance.alpha[i] = 1;
    }
    return instance;
}

void destroy_instance(struct Instance instance)
{
    if (instance.alpha)
        free(instance.alpha);

    if (instance.veto)
        free(instance.veto);

    if (instance.beta)
        free(instance.beta);

    if (instance.weight)
        free(instance.weight);

    if (instance.lambda)
        free(instance.lambda);
}