#include <stdio.h>
#include "interval.h"
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

    return 0;
}
