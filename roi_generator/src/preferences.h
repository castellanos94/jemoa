#include "dominance.h"
#include "instance.h"
#ifndef PREFERENCES_H
#define PREFERENCES_H
/*ETA-Dominance:  Definition 3. Relatiopships: xS(δ,λ)y in [-2], xP(δ,λ)y in [-1], xI(δ,λ)y
      in [0], xR(δ,λ)y in [1], sigmaXY, sigmaYX*/
double *compare_by_preferences(struct Instance instance, int dm, struct Interval a[], struct Interval b[]);
#endif // TYPES_H