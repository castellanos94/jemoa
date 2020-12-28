#include "dtlz.h"
#include <stdlib.h>
#include <math.h>
#include <string.h>

const double THRESHOLD = 10e-3;
/* generate a random floating point number from min to max */
double randfrom(double min, double max)
{
    double range = (max - min);
    double div = RAND_MAX / range;
    return min + (rand() / div);
}
struct Solution *generateAnalyticalSolution(int numberOfProblem, int numberOfVariables, int numberOfObjectives)
{

    struct Solution solution = init_solution(numberOfVariables, numberOfObjectives);
    int k = numberOfVariables - numberOfObjectives + 1;
    if (numberOfProblem <= 5 && numberOfProblem > 0)
    {

        for (int i = numberOfObjectives - 1; i < numberOfVariables; i++)
        {
            solution.variable[i] = 0.5;
        }
    }
    double sum;
    if (numberOfProblem == 1)
    {
        do
        {
            for (int i = 0; i < numberOfObjectives - 1; i++)
            {
                solution.variable[i] = randfrom(0, 1);
            }
            evaluateSolution(numberOfProblem, solution);
            sum = 0;
            for (int i = 0; i < numberOfObjectives; i++)
            {
                sum += solution.objective[i];
            }
        } while (sum != 0.5);
    }
    else if (numberOfProblem > 1 && numberOfProblem < 7)
    {

        do
        {
            for (int i = 0; i < numberOfObjectives - 1; i++)
            {
                solution.variable[i] = randfrom(0, 1);
            }
            evaluateSolution(numberOfProblem, solution);
            sum = 0;
            for (int i = 0; i < numberOfObjectives; i++)
            {
                sum += solution.objective[i] * solution.objective[i];
            }
        } while (sum != 1.0);
    }
    else
    {
        for (int i = 0; i < numberOfObjectives - 1; i++)
        {
            solution.variable[i] = randfrom(0, 1);
        }
        evaluateSolution(numberOfProblem, solution);
    }
    struct Solution *tmp = malloc(sizeof(solution));
    memcpy(tmp, &solution, sizeof(solution));
    return tmp;
}

double g(struct Solution solution)
{
    double g = 0.0;
    int k = solution.numberOfVariables - solution.numberOfObjectives + 1;

    for (int i = solution.numberOfVariables - k; i < solution.numberOfVariables; i++)
    {
        g += (solution.variable[i] - 0.5) * (solution.variable[i] - 0.5) - cos(20.0 * M_PI * (solution.variable[i] - 0.5));
    }

    g = 100 * (k + g);
    return g;
}
double g2(struct Solution solution)
{
    double g = 0.0;
    int k = solution.numberOfVariables - solution.numberOfObjectives + 1;

    for (int i = solution.numberOfVariables - k; i < solution.numberOfVariables; i++)
    {
        g += (solution.variable[i] - 0.5) * (solution.variable[i] - 0.5);
    }
    return g;
}
void evaluateDTLZ1(struct Solution solution)
{
    double f[solution.numberOfObjectives];
    double _g = g(solution);
    for (int i = 0; i < solution.numberOfObjectives; i++)
    {
        f[i] = (1.0 + _g) * 0.5;
    }

    for (int i = 0; i < solution.numberOfObjectives; i++)
    {
        for (int j = 0; j < solution.numberOfObjectives - (i + 1); j++)
        {
            f[i] *= solution.variable[j];
        }
        if (i != 0)
        {
            int aux = solution.numberOfObjectives - (i + 1);
            f[i] *= 1 - solution.variable[aux];
        }
    }

    for (int i = 0; i < solution.numberOfObjectives; i++)
    {
        solution.objective[i] = f[i];
    }
}
void evaluateDTLZ2(struct Solution solution)
{

    double f[solution.numberOfObjectives];

    double _g = g2(solution);

    for (int i = 0; i < solution.numberOfObjectives; i++)
    {
        f[i] = 1.0 + _g;
    }

    for (int i = 0; i < solution.numberOfObjectives; i++)
    {
        for (int j = 0; j < solution.numberOfObjectives - (i + 1); j++)
        {
            f[i] *= cos(solution.variable[j] * 0.5 * M_PI);
        }
        if (i != 0)
        {
            int aux = solution.numberOfObjectives - (i + 1);
            f[i] *= sin(solution.variable[aux] * 0.5 * M_PI);
        }
    }

    for (int i = 0; i < solution.numberOfObjectives; i++)
    {
        solution.objective[i] = f[i];
    }
}

void evaluateDTLZ3(struct Solution solution)
{

    double f[solution.numberOfObjectives];

    double _g = g(solution);

    for (int i = 0; i < solution.numberOfObjectives; i++)
    {
        f[i] = 1.0 + _g;
    }

    for (int i = 0; i < solution.numberOfObjectives; i++)
    {
        for (int j = 0; j < solution.numberOfObjectives - (i + 1); j++)
        {
            f[i] *= cos(solution.variable[j] * 0.5 * M_PI);
        }
        if (i != 0)
        {
            int aux = solution.numberOfObjectives - (i + 1);
            f[i] *= sin(solution.variable[aux] * 0.5 * M_PI);
        }
    }

    for (int i = 0; i < solution.numberOfObjectives; i++)
    {
        solution.objective[i] = f[i];
    }
}

void evaluateDTLZ4(struct Solution solution)
{

    double f[solution.numberOfObjectives];
    double alpha = 100;
    double _g = g2(solution);

    for (int i = 0; i < solution.numberOfObjectives; i++)
    {
        f[i] = 1.0 + _g;
    }

    for (int i = 0; i < solution.numberOfObjectives; i++)
    {
        for (int j = 0; j < solution.numberOfObjectives - (i + 1); j++)
        {
            f[i] *= cos(pow(solution.variable[j], alpha) * (M_PI / 2.0));
        }
        if (i != 0)
        {
            int aux = solution.numberOfObjectives - (i + 1);
            f[i] *= sin(pow(solution.variable[aux], alpha) * (M_PI / 2.0));
        }
    }

    for (int i = 0; i < solution.numberOfObjectives; i++)
    {
        solution.objective[i] = f[i];
    }
}

void evaluateDTLZ5(struct Solution solution)
{

    double f[solution.numberOfObjectives];
    double theta[solution.numberOfObjectives - 1];

    double _g = g2(solution);

    double t = M_PI / (4.0 * (1.0 + _g));

    theta[0] = solution.variable[0] * M_PI / 2.0;
    for (int i = 1; i < (solution.numberOfObjectives - 1); i++)
    {
        theta[i] = t * (1.0 + 2.0 * _g * solution.variable[i]);
    }

    for (int i = 0; i < solution.numberOfObjectives; i++)
    {
        f[i] = 1.0 + _g;
    }

    for (int i = 0; i < solution.numberOfObjectives; i++)
    {
        for (int j = 0; j < solution.numberOfObjectives - (i + 1); j++)
        {
            f[i] *= cos(theta[j]);
        }
        if (i != 0)
        {
            int aux = solution.numberOfObjectives - (i + 1);
            f[i] *= sin(theta[aux]);
        }
    }
    for (int i = 0; i < solution.numberOfObjectives; i++)
    {
        solution.objective[i] = f[i];
    }
}

void evaluateDTLZ6(struct Solution solution)
{

    double f[solution.numberOfObjectives];
    double theta[solution.numberOfObjectives - 1];
    double _g = 0.0;
    int k = solution.numberOfVariables - solution.numberOfObjectives + 1;

    for (int i = solution.numberOfVariables - k; i < solution.numberOfVariables; i++)
    {
        _g += pow(solution.variable[i], 0.1);
    }

    double t = M_PI / (4.0 * (1.0 + _g));
    theta[0] = solution.variable[0] * M_PI / 2;
    for (int i = 1; i < (solution.numberOfObjectives - 1); i++)
    {
        theta[i] = t * (1.0 + 2.0 * _g * solution.variable[i]);
    }

    for (int i = 0; i < solution.numberOfObjectives; i++)
    {
        f[i] = 1.0 + _g;
    }

    for (int i = 0; i < solution.numberOfObjectives; i++)
    {
        for (int j = 0; j < solution.numberOfObjectives - (i + 1); j++)
        {
            f[i] *= cos(theta[j]);
        }
        if (i != 0)
        {
            int aux = solution.numberOfObjectives - (i + 1);
            f[i] *= sin(theta[aux]);
        }
    }

    for (int i = 0; i < solution.numberOfObjectives; i++)
    {
        solution.objective[i] = f[i];
    }
}

void evaluateDTLZ7(struct Solution solution)
{

    double f[solution.numberOfObjectives];
    int k = solution.numberOfVariables - solution.numberOfObjectives + 1;

    double g = 0.0;
    for (int i = solution.numberOfVariables - k; i < solution.numberOfVariables; i++)
    {
        g += solution.variable[i];
    }

    g = 1 + (9.0 * g) / k;
    //System.arraycopy(x, 0, f, 0, numberOfObjectives - 1);
    for (int i = 0; i < solution.numberOfObjectives - 1; i++)
    {
        f[i] = solution.variable[i];
    }

    double h = 0.0;
    for (int i = 0; i < solution.numberOfObjectives - 1; i++)
    {
        h += (f[i] / (1.0 + g)) * (1 + sin(3.0 * M_PI * f[i]));
    }

    h = solution.numberOfObjectives - h;

    f[solution.numberOfObjectives - 1] = (1 + g) * h;

    for (int i = 0; i < solution.numberOfObjectives; i++)
    {
        solution.objective[i] = f[i];
    }
}

void evaluateSolution(int problem, struct Solution solution)
{
    switch (problem)
    {
    case 1:
        evaluateDTLZ1(solution);
        break;
    case 2:
        evaluateDTLZ2(solution);
        break;
    case 3:
        evaluateDTLZ3(solution);
        break;
    case 4:
        evaluateDTLZ4(solution);
        break;
    case 5:
        evaluateDTLZ5(solution);
        break;
    case 6:
        evaluateDTLZ6(solution);
        break;
    case 7:
        evaluateDTLZ7(solution);
        break;
    default:
        break;
    }
}
