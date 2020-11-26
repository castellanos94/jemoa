package com.castellanos94.utils;

public enum POST_HOC {
    NEMENYI,
    /**
     * Holm: It compares each pi (starting from the most significant or the lowest)
     * with: α(K−i), where i∈[1,K−1]. If the hypothesis is rejected the test
     * continues the comparisons. When an hypothesis is accepted, all the other
     * hypothesis are accepted as well. It is better (more power) than
     * Bonferroni-Dunn test, because it controls the FWER (familywise error rate),
     * which is the probability of committing one or more type I errors among all
     * hypothesis.
     */
    HOLM,
    /**
     * Finner: Finner's test is similar to Holm's but each p-value associated with
     * the hypothesis Hi is compared with: pi≤1−(1−α)(K−1)i, where i∈[1,K−1]. It is
     * more powerful than Bonferroni-Dunn, Holm, Hochberg and Li (only in some
     * cases).
     */
    FINNER,
    /**
     * Hochberg: It compares in the opposite direction to Holm. As soon as an
     * acceptable hypothesis is found, all the other hypothesis are accepted. It is
     * better (more power) than Holm test, but the differences between them are
     * small in practice.
     */
    HOCHBERG,
    /**
     * Shaffer: This test is like Holm's but each p-value associated with the
     * hypothesis Hi is compared as pi≤αti, where ti is the maximum number of
     * possible hypothesis assuming that the previous (j−1) hypothesis have been
     * rejected.
     */
    SHAFFER;

    @Override
    public String toString() {
        switch (this) {
            case HOLM:
                return "holm_multitest";
            case FINNER:
                return "finner_multitest";
            case NEMENYI:
                return "nemenyi_multitest";
            case SHAFFER:
                return "shaffer_multitest";
            case HOCHBERG:
                return "hochberg_multitest";
            default:
                return null;
        }

    }
}
