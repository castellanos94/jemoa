#include "dominance.h"
int dominance(int numberOfObjectives, double *a, double *b)
{
    int a_is_best = -1, b_is_best = -1;
    for (int i = 0; i < numberOfObjectives; i++)
    {
        if (a[i] < b[i])
        {
            a_is_best = 1;
        }
        else if (b[i] < a[i])
        {
            b_is_best = 1;
        }
    }
    if (a_is_best == b_is_best)
        return 0;
    if (a_is_best == 1)
        return -1;
    return 1;
}

int interval_dominance(int numberOfObjectives, double alpha, struct Interval a[], struct Interval b[])
{
    int better_a = 0, better_b = 0;
    int strictly_greater_a = 0, strictly_greater_b = 0;
    for (int i = 0; i < numberOfObjectives; i++)
    {
        double possibility_ = possibility(b[i], a[i]);
        if (possibility_ >= alpha)
        {
            if (strictly_greater_a != 1 && possibility_ > 0.5)
            {
                strictly_greater_a = 1;
            }
            better_a++;
        }
        possibility_ = possibility(a[i], b[i]);
        if (possibility_ >= alpha)
        {
            if (strictly_greater_b != 1 && possibility_ > 0.5)
            {
                strictly_greater_b = 1;
            }
            better_b += 1;
        }
    }
    int a_dominates_b = 0, b_dominates_a = 0;
    if (strictly_greater_a == 1 && better_a == numberOfObjectives)
    {
        a_dominates_b = 1;
    }
    if (strictly_greater_b == 1 && better_b == numberOfObjectives)
    {
        b_dominates_a = 1;
    }
    if (a_dominates_b == 1 && b_dominates_a == 1)
    {
        return 0;
    }
    if (a_dominates_b == 1)
    {
        return -1;
    }
    if (b_dominates_a == 1)
    {
        return 1;
    }
    return 0;
}