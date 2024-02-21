package MergeSort;

import java.util.Arrays;

/**
 * Sequential version of Merge sort
 */
public class MergeSortSequential {

    static long[] testArray;

    public static void main(String[] args) {
        System.out.println("MergeSortSequential");
        long startTime = System.currentTimeMillis();

        testArray = ArrayUtil.getArrayFromFile("int.array1m.txt");

        // System.out.println("Initial: " + ArrayUtil.arrayToString(testArray));

        mergeSort(testArray);

        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;
        System.out.println("Execution time: " + executionTime/1000.0 + "s");
        // System.out.println("Sorted: " + ArrayUtil.arrayToString(testArray));
    }

    public static void mergeSort(long[] arr) {
        if (arr.length <= 1) return; // base case

        int mid = arr.length / 2;

        // divide array for two halfes
        long[] left = Arrays.copyOfRange(arr, 0, mid);
        long[] right = Arrays.copyOfRange(arr, mid, arr.length);

        mergeSort(left);
        mergeSort(right);
        merge(left, right, arr);
    }

    public static void merge(long[] left, long[] right, long[] result) {

        int l = 0, r = 0, i = 0;
        int leftBorder = left.length;
        int rightBorder = right.length;

        while(l < leftBorder && r < rightBorder){
            if (left[l] < right[r]) {
                result[i] = left[l];
                l++; i++;
            } else {
                result[i] = right[r];
                r++; i++;
            }
        }

        while (l < leftBorder){
            result[i] = left[l];
            l++;
            i++;
        }

        while(r < rightBorder){
            result[i] = right[r];
            r++;
            i++;
        }
    }

}
