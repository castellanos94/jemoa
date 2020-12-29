#include "interval.h"
#ifndef DOMINANCE_H
#define DOMINANCE_H
/* Pareto Dominance : minimization*/
int dominance(int numberOfObjectives, double *a, double *b);
/* ITHDM Dominance : minimization */
int interval_dominance(int numberOfObjectives, double alpha, struct Interval a[], struct Interval b[]);
#endif // TYPES_H