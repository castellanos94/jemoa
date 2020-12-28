#include "solution.h"
#ifndef DTLZPROBLEM_H
#define DTLZPROBLEM_H
struct Solution *generateAnalyticalSolution(int numberOfProblem, int numberOfVariables, int numberOfObjectives);
void evaluateSolution(int problem,struct Solution solution);
/* generate a random floating point number from min to max */
double randfrom(double min, double max);
#endif // TYPES_H