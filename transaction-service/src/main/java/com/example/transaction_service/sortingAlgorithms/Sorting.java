// java
package com.example.transaction_service.sortingAlgorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Sorting {

    /* ---------- Helpers ---------- */

    private static void swap(int[] a, int i, int j) {
        int t = a[i]; a[i] = a[j]; a[j] = t;
    }

    private static <T> void swap(T[] a, int i, int j) {
        T t = a[i]; a[i] = a[j]; a[j] = t;
    }

    /* ---------- Simple O(n^2) sorts ---------- */

    // Bubble Sort
    public static void bubbleSort(int[] a) {
        int n = a.length;
        boolean swapped;
        for (int i = 0; i < n - 1; i++) {
            swapped = false;
            for (int j = 0; j < n - 1 - i; j++) {
                if (a[j] > a[j + 1]) {
                    swap(a, j, j + 1);
                    swapped = true;
                }
            }
            if (!swapped) break;
        }
    }

    // Selection Sort
    public static void selectionSort(int[] a) {
        int n = a.length;
        for (int i = 0; i < n - 1; i++) {
            int minIdx = i;
            for (int j = i + 1; j < n; j++) {
                if (a[j] < a[minIdx]) minIdx = j;
            }
            swap(a, i, minIdx);
        }
    }

    // Insertion Sort
    public static void insertionSort(int[] a) {
        for (int i = 1; i < a.length; i++) {
            int key = a[i];
            int j = i - 1;
            while (j >= 0 && a[j] > key) {
                a[j + 1] = a[j];
                j--;
            }
            a[j + 1] = key;
        }
    }

    /* ---------- Shell Sort (gap sequence: n/2) ---------- */

    public static void shellSort(int[] a) {
        int n = a.length;
        for (int gap = n / 2; gap > 0; gap /= 2) {
            for (int i = gap; i < n; i++) {
                int temp = a[i];
                int j = i;
                while (j >= gap && a[j - gap] > temp) {
                    a[j] = a[j - gap];
                    j -= gap;
                }
                a[j] = temp;
            }
        }
    }

    /* ---------- Merge Sort (stable) ---------- */

    public static void mergeSort(int[] a) {
        if (a == null || a.length < 2) return;
        int[] aux = Arrays.copyOf(a, a.length);
        mergeSort(a, aux, 0, a.length - 1);
    }

    private static void mergeSort(int[] a, int[] aux, int l, int r) {
        if (l >= r) return;
        int m = l + (r - l) / 2;
        mergeSort(a, aux, l, m);
        mergeSort(a, aux, m + 1, r);
        merge(a, aux, l, m, r);
    }

    private static void merge(int[] a, int[] aux, int l, int m, int r) {
        System.arraycopy(a, l, aux, l, r - l + 1);
        int i = l, j = m + 1, k = l;
        while (i <= m && j <= r) {
            if (aux[i] <= aux[j]) a[k++] = aux[i++];
            else a[k++] = aux[j++];
        }
        while (i <= m) a[k++] = aux[i++];
        while (j <= r) a[k++] = aux[j++];
    }

    /* ---------- Quick Sort (in-place) ---------- */

    public static void quickSort(int[] a) {
        quickSort(a, 0, a.length - 1);
    }

    private static void quickSort(int[] a, int low, int high) {
        if (low >= high) return;
        int p = partition(a, low, high);
        quickSort(a, low, p - 1);
        quickSort(a, p + 1, high);
    }

    private static int partition(int[] a, int low, int high) {
        int pivot = a[high];
        int i = low;
        for (int j = low; j < high; j++) {
            if (a[j] <= pivot) {
                swap(a, i, j);
                i++;
            }
        }
        swap(a, i, high);
        return i;
    }

    /* ---------- Heap Sort ---------- */

    public static void heapSort(int[] a) {
        int n = a.length;
        // build max heap
        for (int i = n / 2 - 1; i >= 0; i--) heapify(a, n, i);
        // extract elements
        for (int i = n - 1; i > 0; i--) {
            swap(a, 0, i);
            heapify(a, i, 0);
        }
    }

    private static void heapify(int[] a, int heapSize, int root) {
        int largest = root;
        int l = 2 * root + 1;
        int r = 2 * root + 2;
        if (l < heapSize && a[l] > a[largest]) largest = l;
        if (r < heapSize && a[r] > a[largest]) largest = r;
        if (largest != root) {
            swap(a, root, largest);
            heapify(a, heapSize, largest);
        }
    }

    /* ---------- Counting Sort (for non\-negative integers) ---------- */

    public static void countingSort(int[] a) {
        if (a.length == 0) return;
        int max = a[0];
        for (int v : a) {
            if (v < 0) throw new IllegalArgumentException("countingSort requires non-negative ints");
            if (v > max) max = v;
        }
        int[] counts = new int[max + 1];
        for (int v : a) counts[v]++;
        int idx = 0;
        for (int i = 0; i <= max; i++) {
            while (counts[i]-- > 0) a[idx++] = i;
        }
    }

    /* ---------- Radix Sort (LSD, base 10, for non\-negative integers) ---------- */

    public static void radixSort(int[] a) {
        if (a.length == 0) return;
        int max = Arrays.stream(a).max().orElse(0);
        if (max < 0) throw new IllegalArgumentException("radixSort requires non-negative ints");
        int exp = 1;
        int[] aux = new int[a.length];
        while (max / exp > 0) {
            int[] counts = new int[10];
            for (int v : a) counts[(v / exp) % 10]++;
            for (int i = 1; i < 10; i++) counts[i] += counts[i - 1];
            for (int i = a.length - 1; i >= 0; i--) {
                int digit = (a[i] / exp) % 10;
                aux[--counts[digit]] = a[i];
            }
            System.arraycopy(aux, 0, a, 0, a.length);
            exp *= 10;
        }
    }

    /* ---------- Bucket Sort for doubles in [0,1) ---------- */

    public static void bucketSort(double[] a) {
        int n = a.length;
        List<List<Double>> buckets = new ArrayList<>(n);
        for (int i = 0; i < n; i++) buckets.add(new ArrayList<>());
        for (double v : a) {
            if (v < 0.0 || v >= 1.0) throw new IllegalArgumentException("bucketSort expects values in [0,1)");
            int idx = (int) (v * n);
            buckets.get(idx).add(v);
        }
        int pos = 0;
        for (List<Double> bucket : buckets) {
            bucket.sort(Double::compareTo);
            for (double v : bucket) a[pos++] = v;
        }
    }

    /* ---------- Generic variants for Comparable arrays ---------- */

    public static <T extends Comparable<? super T>> void insertionSort(T[] a) {
        for (int i = 1; i < a.length; i++) {
            T key = a[i];
            int j = i - 1;
            while (j >= 0 && a[j].compareTo(key) > 0) {
                a[j + 1] = a[j];
                j--;
            }
            a[j + 1] = key;
        }
    }

    public static <T extends Comparable<? super T>> void mergeSort(T[] a) {
        if (a == null || a.length < 2) return;
        T[] aux = Arrays.copyOf(a, a.length);
        mergeSort(a, aux, 0, a.length - 1);
    }

    private static <T extends Comparable<? super T>> void mergeSort(T[] a, T[] aux, int l, int r) {
        if (l >= r) return;
        int m = l + (r - l) / 2;
        mergeSort(a, aux, l, m);
        mergeSort(a, aux, m + 1, r);
        merge(a, aux, l, m, r);
    }

    private static <T extends Comparable<? super T>> void merge(T[] a, T[] aux, int l, int m, int r) {
        System.arraycopy(a, l, aux, l, r - l + 1);
        int i = l, j = m + 1, k = l;
        while (i <= m && j <= r) {
            if (aux[i].compareTo(aux[j]) <= 0) a[k++] = aux[i++];
            else a[k++] = aux[j++];
        }
        while (i <= m) a[k++] = aux[i++];
        while (j <= r) a[k++] = aux[j++];
    }

    public static <T extends Comparable<? super T>> void quickSort(T[] a) {
        quickSort(a, 0, a.length - 1);
    }

    private static <T extends Comparable<? super T>> void quickSort(T[] a, int low, int high) {
        if (low >= high) return;
        int p = partition(a, low, high);
        quickSort(a, low, p - 1);
        quickSort(a, p + 1, high);
    }

    private static <T extends Comparable<? super T>> int partition(T[] a, int low, int high) {
        T pivot = a[high];
        int i = low;
        for (int j = low; j < high; j++) {
            if (a[j].compareTo(pivot) <= 0) {
                swap(a, i, j);
                i++;
            }
        }
        swap(a, i, high);
        return i;
    }
}
