package MergeSort;

import java.util.Arrays;

/**
 * sequential implementation of merge sort
 * provided by Concurrency course authors
 */
public class SequentialMergeSorterV3 {
    private int[] array;

    public SequentialMergeSorterV3(int[] array) {
        this.array = array;
    }

    /* returns sorted array */
    public int[] sort() {
        sort(0, array.length-1);
        return array;
    }

    /* helper method that gets called recursively */
    private void sort(int left, int right) {
        if (left < right) {
            int mid = (left+right)/2; // find the middle point
            sort(left, mid); // sort the left half
            sort(mid+1, right); // sort the right half
            merge(left, mid, right); // merge the two sorted halves
        }
    }

    /* helper method to merge two sorted subarrays array[l..m] and array[m+1..r] into array */
    private void merge(int left, int mid, int right) {
        // copy data to temp subarrays to be merged
        int leftTempArray[] = Arrays.copyOfRange(array, left, mid+1);
        int rightTempArray[] = Arrays.copyOfRange(array, mid+1, right+1);

        // initial indexes for left, right, and merged subarrays
        int leftTempIndex=0, rightTempIndex=0, mergeIndex=left;

        // merge temp arrays into original
        while (leftTempIndex < mid - left + 1 || rightTempIndex < right - mid) {
            if (leftTempIndex < mid - left + 1 && rightTempIndex < right - mid) {
                if (leftTempArray[leftTempIndex] <= rightTempArray[rightTempIndex]) {
                    array[mergeIndex ] = leftTempArray[leftTempIndex];
                    leftTempIndex++;
                } else {
                    array[mergeIndex ] = rightTempArray[rightTempIndex];
                    rightTempIndex++;
                }
            } else if (leftTempIndex < mid - left + 1) { // copy any remaining on left side
                array[mergeIndex ] = leftTempArray[leftTempIndex];
                leftTempIndex++;
            } else if (rightTempIndex < right - mid) { // copy any remaining on right side
                array[mergeIndex ] = rightTempArray[rightTempIndex];
                rightTempIndex++;
            }
            mergeIndex++;
        }
    }
}
