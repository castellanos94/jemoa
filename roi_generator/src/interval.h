#include <stdio.h>
#ifndef INTERVAL_H
#define INTERVAL_H
struct Interval
{
    double lower;
    double upper;
};
typedef struct Interval interval;

struct Interval plus(struct Interval a, struct Interval b);
struct Interval minus(struct Interval a, struct Interval b);
struct Interval times(struct Interval a, struct Interval b);
struct Interval division(struct Interval a, struct Interval b);
int compareTo(struct Interval a, struct Interval b);
double possGreaterThanOrEq(struct Interval a, struct Interval b);
double possSmallerThanOrEq(struct Interval a, struct Interval b);
double possibility(struct Interval a, struct Interval b);
char* toString(struct Interval number);
#endif // TYPES_H