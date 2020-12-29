#include "interval.h"
#include <math.h>
#include <stdlib.h>

struct Interval plus(struct Interval a, struct Interval b)
{
    struct Interval data = {.lower = a.lower + b.lower, .upper = a.upper + b.upper};
    return data;
}
struct Interval minus(struct Interval a, struct Interval b)
{
    struct Interval data = {.lower = a.lower - b.upper, .upper = a.upper - b.lower};
    return data;
}

struct Interval times(struct Interval a, struct Interval b)
{
    double da = a.lower * b.lower, db = a.lower * b.upper, dc = a.upper * b.lower, dd = a.upper * b.upper;
    double lower = (da < db) ? da : db;
    lower = (lower < dc) ? lower : dc;
    lower = (lower < dd) ? lower : dd;
    double upper = (da > db) ? da : db;
    upper = (upper > dc) ? upper : dc;
    upper = (upper > dd) ? upper : dd;
    struct Interval data = {.lower = lower, .upper = upper};
    return data;
}

struct Interval division(struct Interval a, struct Interval b)
{
    double c = b.lower;
    double d = b.upper;
    struct Interval data; // = {.lower = 0, .upper = 0};
    if (c == 0 && d > 0)
    {
        data.lower = 1.0 / d;
        data.upper = INFINITY;
        return data;
    }
    if (c < d && d == 0)
    {
        data.lower = -INFINITY;
        data.upper = 1.0 / c;
        return data;
    }
    data.lower = 1.0 / c;
    data.upper = 1.0 / d;

    return times(a, data);
}

double possibility(struct Interval a, struct Interval b)
{
    if (a.lower == b.lower && a.upper == b.upper)
    {
        return 0;
    }
    if (a.lower == a.upper && b.lower == b.upper)
    {
        if (a.lower >= b.upper)
            return 1;
        return 0;
    }
    double v = (a.upper - a.lower) + (b.upper - b.lower);

    double ped = (a.upper - b.lower) / v;
    return (ped > 1.0) ? 1.0 : (ped <= 0) ? 0 : ped;
}

int compareTo(struct Interval a, struct Interval b)
{
    if (a.lower == a.upper && b.lower == b.upper)
    {
        if (a.upper == b.upper)
            return 0;
        if (a.upper > b.upper)
            return 1;
        if (a.upper < b.upper)
            return -1;
    }
    double ped = possibility(a, b);
    return (ped == 0.5) ? 0 : (ped < 0.5) ? -1 : 1;
}

double possGreaterThanOrEq(struct Interval a, struct Interval b)
{
    double ped = possibility(a, b);
    return (ped <= 0) ? 0 : (ped >= 1) ? 1 : ped;
}
double possSmallerThanOrEq(struct Interval a, struct Interval b)
{
    return 1.0 - possGreaterThanOrEq(a, b);
}
char* toString(struct Interval number){
    char *str = malloc(sizeof(char)*50);
    sprintf(str,"%f %f", number.lower, number.upper);
    return str;
}