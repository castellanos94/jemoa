#include "dtlz.h"
#include <stdlib.h>
#include <math.h>
#include <string.h>
#include <stdio.h>

const double THRESHOLD = 10e-3;
float round_(float var)
{
    // we use array of chars to store number
    // as a string.
    char str[40];

    // Print in string the value of var
    // with two decimal point
    sprintf(str, "%.3f", var);
    // scan string value in var
    sscanf(str, "%f", &var);

    return var;
}
/* generate a random floating point number from min to max */
double randfrom(double min, double max)
{
    double range = (max - min);
    double div = RAND_MAX / range;
    return round_(min + (rand() / div));
}
int constraintDTLZ8(struct Solution solution)
{
    double rand = randfrom(0, 1);
    int wasStraightLine = 0;
    if (rand < 0.5)
    {
        rand = randfrom(0, 1);
        for (int i = 0; i < solution.numberOfObjectives - 1; i++)
        {
            solution.objective[i] = rand;
        }
        solution.objective[solution.numberOfObjectives - 1] = 1 - 4 * rand;
        wasStraightLine = 1;
    }
    else
    {
        double valida;
        //NOTA: Con 3 objetivos se debe hacer un ajuste ya que RandomNumber.nextInt(M - 2) devuelve un numero entre [0 y M-2] que seria [0 y 1] peroo para el 1 la proabilidad es baja, cambie [M-2] por [M-1] en este caso momentaneamente en las siguienes 6 lineas
        int i = randfrom(0, solution.numberOfObjectives - 1); //Antes era [M - 2] pero la probabilidad de generar [M - 1] es muy baja a comparacion de los otros

        while (i == solution.numberOfObjectives - 1)
        { //[M - 1] es invalido, si cae se vuelve a generar otro
            i = randfrom(0, solution.numberOfObjectives - 1);
        }

        int j = randfrom(0, solution.numberOfObjectives - 1);

        while (j == i || j == solution.numberOfObjectives - 1)
        {
            j = randfrom(0, solution.numberOfObjectives - 1);
        }
        do
        {

            double fi = randfrom(0, 1) * (0.5 - 1.0 / 8) + 1.0 / 8;
            double fj = randfrom(0, 1) * (1 - 2 * fi) + fi;
            for (int o = 0; o < solution.numberOfObjectives - 1; o++)
            {
                if (o == i)
                {
                    solution.objective[o] = fi;
                }
                else if (o == j)
                {
                    solution.objective[o] = fj;
                }
                else
                {
                    solution.objective[o] = randfrom(0, 1) * (1 - fj) + fj;
                }
            }

            solution.objective[solution.numberOfObjectives - 1] = (1 - fi - fj) / 2.0;
            valida = solution.objective[solution.numberOfObjectives - 1] + 4 * fi;
        } while (valida < (1.0 - 1.e-08));
    }

    double g[solution.numberOfObjectives];
    double min = 999 * 10000.0;
    for (int j = 0; j < solution.numberOfObjectives - 1; j++)
    {
        g[j] = solution.objective[solution.numberOfObjectives - 1] + 4 * solution.objective[j] - 1;
        for (int i = 0; i < solution.numberOfObjectives - 1; i++)
        {
            if (i != j)
            {
                double tmp = solution.objective[i] + solution.objective[j];
                if (min > tmp)
                {
                    min = tmp;
                }
            }
        }
    }
    g[solution.numberOfObjectives - 1] = 2 * solution.objective[solution.numberOfObjectives - 1] + min - 1;
    solution.numberOfPenaltieViolated = 0;
    solution.accumulatedPenaltieViolated = 0;
    for (int i = 0; i < solution.numberOfObjectives; i++)
    {
        if (g[i] < 0)
        {
            solution.numberOfPenaltieViolated += 1;
            solution.accumulatedPenaltieViolated += g[i];
        }
    }
    //Check the straight line intersection
    if (wasStraightLine == 1)
    {
        int M = (solution.numberOfObjectives == 3) ? 1 : solution.numberOfObjectives - 1;
        for (int i = 0; i < M; i++)
        {
            if (solution.objective[i] != solution.objective[i + 1])
            {
                solution.numberOfPenaltieViolated = solution.numberOfPenaltieViolated + 100;
                solution.accumulatedPenaltieViolated = solution.accumulatedPenaltieViolated + 100;
            }
        }
    }
    return solution.numberOfPenaltieViolated;
}
int constraintDTLZ9(struct Solution solution)
{
    double rand = randfrom(0, 1);
    if (rand < 0.5)
    {
        rand = randfrom(0, 1);
        for (int i = 0; i < solution.numberOfObjectives - 1; i++)
        {
            solution.objective[i] = rand;
        }
        solution.objective[solution.numberOfObjectives - 1] = sqrt(1 - rand * rand);
    }
    else
    {

        double valida;
        //NOTA: Con 3 objetivos se debe hacer un ajuste ya que RandomNumber.nextInt(M - 2) devuelve un numero entre [0 y M-2] que seria [0 y 1] peroo para el 1 la proabilidad es baja, cambie [M-2] por [M-1] en este caso momentaneamente en las siguienes 6 lineas
        int i = randfrom(0, solution.numberOfObjectives - 1); //Antes era [M - 2] pero la probabilidad de generar [M - 1] es muy baja a comparacion de los otros

        while (i == solution.numberOfObjectives - 1)
        { //[M - 1] es invalido, si cae se vuelve a generar otro
            i = randfrom(0, solution.numberOfObjectives - 1);
        }

        int j = randfrom(0, solution.numberOfObjectives - 1);

        while (j == i || j == solution.numberOfObjectives - 1)
        {
            j = randfrom(0, solution.numberOfObjectives - 1);
        }
        do
        {

            double fi = randfrom(0, 1) * (0.5 - 1.0 / 8) + 1.0 / 8;
            double fj = randfrom(0, 1) * (1 - 2 * fi) + fi;
            for (int o = 0; o < solution.numberOfObjectives - 1; o++)
            {
                if (o == i)
                {
                    solution.objective[o] = fi;
                }
                else if (o == j)
                {
                    solution.objective[o] = fj;
                }
                else
                {
                    solution.objective[o] = randfrom(0, 1) * (1 - fj) + fj;
                }
            }

            solution.objective[solution.numberOfObjectives - 1] = (1 - fi - fj) / 2.0;
            valida = solution.objective[solution.numberOfObjectives - 1] + 4 * fi;
        } while (valida < (1.0 - 1.e-08));
    }
    double g[solution.numberOfObjectives];
    double fm = solution.objective[solution.numberOfObjectives - 1];
    fm = fm * fm;
    solution.numberOfPenaltieViolated = 0;
    solution.accumulatedPenaltieViolated = 0;
    for (int j = 0; j < solution.numberOfObjectives; j++)
    {
        double fj = solution.objective[j];
        g[j] = fm + fj * fj - 1;
        if (g[j] < 0)
        {
            solution.numberOfPenaltieViolated += 1;
            solution.accumulatedPenaltieViolated += g[j];
        }
    }

    return solution.numberOfPenaltieViolated;
}

struct Solution *generateAnalyticalSolution(int numberOfProblem, int numberOfVariables, int numberOfObjectives)
{

    struct Solution solution = init_solution(numberOfVariables, numberOfObjectives);
    int k = numberOfVariables - numberOfObjectives + 1;
    if (numberOfProblem <= 5)
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
        if (numberOfProblem > 7)
        {
            int numberOfViolatedPenalties;
            if (numberOfProblem == 8)
            {
                do
                {
                    numberOfViolatedPenalties = constraintDTLZ8(solution);
                } while (numberOfViolatedPenalties != 0);
            }
            else if (numberOfProblem == 9)
            {
                do
                {
                    numberOfViolatedPenalties = constraintDTLZ9(solution);
                } while (numberOfViolatedPenalties != 0);
            }
        }
        else
        {
            for (int i = 0; i < numberOfObjectives - 1; i++)
            {
                solution.variable[i] = randfrom(0, 1);
            }
            evaluateSolution(numberOfProblem, solution);
        }
    }
    struct Solution *tmp = malloc(sizeof(solution));
    memcpy(tmp, &solution, sizeof(solution));
    tmp->accumulatedPenaltieViolated = solution.accumulatedPenaltieViolated;
    tmp->numberOfPenaltieViolated = solution.numberOfPenaltieViolated;
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
void evaluateDTLZ8(struct Solution solution)
{
    double factorNM = (solution.numberOfVariables * 1.0) / solution.numberOfObjectives;

    for (int j = 0; j < solution.numberOfObjectives; j++)
    {
        int lower = floor(j * factorNM);
        int upper = floor((j + 1.0) * factorNM);
        double sum = 0;
        for (int i = lower; i < upper; i++)
        {
            sum += solution.variable[i];
        }
        solution.objective[j] = (sum / factorNM);
    }
}

void evaluateDTLZ9(struct Solution solution)
{
    double factorNM = (solution.numberOfVariables * 1.0) / solution.numberOfObjectives;
    for (int j = 0; j < solution.numberOfObjectives; j++)
    {
        double sum = 0;
        for (int i = (j * factorNM); i < (j + 1) * factorNM; i++)
        {
            sum += pow(solution.variable[i], 0.1);
        }
        solution.objective[j] = sum;
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
    case 8:
        evaluateDTLZ8(solution);
        break;
    case 9:
        evaluateDTLZ9(solution);
        break;
    default:
        break;
    }
}
