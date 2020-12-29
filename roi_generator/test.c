#include <stdio.h>
#include "src/interval.h"
#include "src/dominance.h"
#include "src/instance.h"
void printInterval(struct Interval a)
{
    printf("%f %f\n", a.lower, a.upper);
}
int main(int argc, char const *argv[])
{
    struct Interval a = {3, 4};
    struct Interval b = {4, 5};
    printInterval(a);
    printInterval(b);
    printInterval(times(a, b));
    printInterval(a);
    printInterval(plus(a, b));
    printInterval(minus(a, b));
    printInterval(division(a, b));
    printf("A compare to B : %d\n", compareTo(a, b));
    printf("A possibility to B : %f\n", possibility(a, b));
    printf("A possibility to greather that B : %f\n", possGreaterThanOrEq(a, b));
    printf("A possibility to smaller that B : %f\n", possSmallerThanOrEq(a, b));
    double alpha = 1.0;
    int numberOfObjectives = 3;
    struct Interval sa[] = {{0.968, 0.968}, {0.24869, 0.24869}, {0, 0}};
    struct Interval sb[] = {{0.99283, 0.99283}, {0.3233, 0.3233}, {0, 0}};
    printf("a <> b ? %3d.\n", interval_dominance(numberOfObjectives,alpha,sa, sb));
    struct Instance dtlz= readInstance("/home/thinkpad/Documents/jemoa/src/main/resources/DTLZ_INSTANCES/DTLZ1_Instance.txt");
    printf("After read instance\n");
    printInstance(dtlz);
    destroy_instance(dtlz);
    return 0;
}
