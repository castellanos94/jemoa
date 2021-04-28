package com.castellanos94.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.castellanos94.solutions.Solution;

public class HeapSort<S extends Solution<?>> {
    protected Comparator<S> cmp;

    public HeapSort(Comparator<S> comparator) {
        this.cmp = comparator;
    }

    public void sort(ArrayList<S> arr) {
        int n = arr.size();

        // Build heap (rearrange array)
        for (int i = n / 2 - 1; i >= 0; i--)
            heapify(arr, n, i);

        // One by one extract an element from heap
        for (int i = n - 1; i > 0; i--) {
            // Move current root to end

            Collections.swap(arr, 0, i);

            // call max heapify on the reduced heap
            heapify(arr, i, 0);
        }
    }

    // To heapify a subtree rooted with node i which is
    // an index in arr[]. n is size of heap
    private void heapify(ArrayList<S> arr, int n, int i) {
        int largest = i; // Initialize largest as root
        int l = 2 * i + 1; // left = 2*i + 1
        int r = 2 * i + 2; // right = 2*i + 2

        // If left child is larger than root

        if (l < n && cmp.compare(arr.get(l), arr.get(largest)) > 0)
            largest = l;

        // If right child is larger than largest so far
        if (r < n && cmp.compare(arr.get(r), arr.get(largest)) > 0)
            largest = r;

        // If largest is not root
        if (largest != i) {
            Collections.swap(arr, i, largest);

            // Recursively heapify the affected sub-tree
            heapify(arr, n, largest);
        }
    }

}
