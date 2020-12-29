#include "interval.h"
#include "stdlib.h"
#ifndef INSTANCE_H
#define INSTANCE_H
struct Instance
{
    int numberOfObjectives;
    int numberOfVariables;
    int numberOfDM;
    struct Interval **weight;
    struct Interval **veto;
    struct Interval *beta;
    struct Interval *lambda;
    double *alpha;
};
typedef struct Instance instance;
void printInstance(struct Instance instance);
struct Instance readInstance(const char *path);
void destroy_instance(struct Instance instance);
#endif // TYPES_H