#ifndef SOLUTIONOPERATIONS_H
#define SOLUTIONOPERATIONS_H
struct Solution
{
    const int numberOfVariables;
    const int numberOfObjectives;
    double *variable;
    double *objective;
    int rank;
};
typedef struct Solution solution;

struct Solution init_solution(int numberOfVariables, int numberOfObjectives);
void printSolution(struct Solution *solutionToPrint);
void destroy_solution(struct Solution *solution);
#endif // TYPES_H