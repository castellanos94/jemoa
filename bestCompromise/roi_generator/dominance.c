int dominance(int n, double *a, double *b)
{
    int a_is_best = -1, b_is_best = -1;
    for (int i = 0; i < n; i++)
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
