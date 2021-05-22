#include "preferences.h"
#include <math.h>
double compute_alpha_ij(struct Interval a, struct Interval b)
{
    double res;
    if (a.lower == a.upper)
    {
        res = (a.lower <= b.lower) ? 1 : 0;
    }
    else
    {
        res = possSmallerThanOrEq(a, b);
    }
    return res;
}
double compute_discordance_ij(struct Interval veto, struct Interval a, struct Interval b)
{
    return possSmallerThanOrEq(b, minus(a, veto));
}
struct Interval concordance_index(struct Instance instance, int dm, double gamma, double omegas[], int coalition[])
{
    double cl = 0, cu = 0, dl = 0, du = 0;
    double lower = 0, upper = 0;
    for (int i = 0; i < instance.numberOfObjectives; i++)
    {
        if (omegas[i] >= gamma)
        {
            coalition[i] = 1;
            cl += instance.weight[dm][i].lower;
            cu += instance.weight[dm][i].upper;
        }
        else
        {
            coalition[i] = 0;
            dl += instance.weight[dm][i].lower;
            du += instance.weight[dm][i].upper;
        }
    }
    if (cl + du >= 1)
    {
        lower = cl;
    }
    else
    {
        lower = 1 - du;
    }
    if (cu + dl <= 1)
    {
        upper = cu;
    }
    else
    {
        upper = 1 - dl;
    }

    struct Interval res = {.lower = lower, .upper = upper};
    return res;
}
double credibility_index(struct Instance instance, int dm, struct Interval a[], struct Interval b[])
{
    double eta_gamma[instance.numberOfObjectives];
    double omegas[instance.numberOfObjectives];

    double max_discordance, non_discordance, max_eta_gamma = -INFINITY;
    struct Interval ci;
    double dj[instance.numberOfObjectives];
    for (int i = 0; i < instance.numberOfObjectives; i++)
    {
        omegas[i] = compute_alpha_ij(a[i], b[i]);
        dj[i] = compute_discordance_ij(instance.veto[dm][i], a[i], b[i]);
    }
    int coalition[instance.numberOfObjectives];
    for (int i = 0; i < instance.numberOfObjectives; i++)
    {
        double gamma = omegas[i];
        ci = concordance_index(instance, dm, gamma, omegas, coalition);
        double poss = possGreaterThanOrEq(ci, instance.lambda[dm]);
        for (int j = 0; j < instance.numberOfObjectives; j++)
        {
            if (coalition[j] == 0 && dj[j] > max_discordance)
            {
                max_discordance = dj[j];
            }
        }
        non_discordance = 1 - max_discordance;
        eta_gamma[i] = gamma;
        if (eta_gamma[i] > poss)
        {
            eta_gamma[i] = poss;
        }
        if (eta_gamma[i] > non_discordance)
        {
            eta_gamma[i] = non_discordance;
        }
        if (max_eta_gamma < eta_gamma[i])
        {
            max_eta_gamma = eta_gamma[i];
        }
    }

    return max_eta_gamma;
}
double *compare_by_preferences(struct Instance instance, int dm, struct Interval a[], struct Interval b[])
{

    double *result = malloc(sizeof(double) * 3);
    result[1] = credibility_index(instance, dm, a, b); // sigmaxy
    result[2] = credibility_index(instance, dm, b, a); // sigmayx
    int v = interval_dominance(instance.numberOfObjectives, instance.alpha[dm], a, b);
    if (v == -1)
    {
        result[0] = -2;
    }
    if (v == 1)
    {
        result[0] = 2;
    }
    //struct Interval sigmaXY = {result[1], result[1]};
    //struct Interval sigmaYX = {result[2], result[2]};
    float sigmaXY = result[1], sigmaYX = result[2];
    float delta = 0.51;
    int xSDelta = sigmaXY >= delta;
    if (xSDelta >= 0)
    {
        if (sigmaYX < delta)
        {
            result[0] = -1;
        }
        else
        {
            result[0] = 0;
        }
    }
    else
    {
        result[0] = 1;
    }
    /*if (compareTo(sigmaXY, instance.beta[dm]) == 0 && compareTo(instance.beta[dm], sigmaYX) > 0)
    {
        result[0] = -1;
    }
    else if (compareTo(sigmaXY, instance.beta[dm]) >= 0 && compareTo(instance.beta[dm], sigmaYX) >= 0)
    {
        result[0] = 0;
    }
    else if (compareTo(sigmaXY, instance.beta[dm]) < 0 && compareTo(instance.beta[dm], sigmaYX) < 0)
    {
        result[0] = 1;
    }*/
    return result;
}