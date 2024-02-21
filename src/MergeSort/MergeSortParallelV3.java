package MergeSort;

import java.util.Arrays;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *  parallel implementation of merge sort
 *  provided by Concurrency course authors
 */
public class MergeSortParallelV3 {
    private int[] array;
    //AtomicInteger workerId = new AtomicInteger(0);

    public MergeSortParallelV3(int[] array) {
        this.array = array;
    }

    /* returns sorted array */
    public int[] sort() {
        int numWorkers = Runtime.getRuntime().availableProcessors();
        ForkJoinPool pool = new ForkJoinPool(numWorkers);
        pool.invoke(new ParallelWorker(0,array.length-1));
        return array;
    }

    /* worker that gets called recursively */
    private class ParallelWorker extends RecursiveAction {

        private int left, right;

        public ParallelWorker(int left, int right) {
            this.left = left;
            this.right = right;

            //System.out.println("Worker " + workerId.incrementAndGet());
        }

        protected void compute() {
            // System.out.println("compute() - I am work in thread: " + Thread.currentThread().getName());

            if (left < right) {
                int mid = (left + right) / 2; // find the middle point
                ParallelWorker leftWorker = new ParallelWorker(left, mid);
                ParallelWorker rightWorker = new ParallelWorker(mid+1, right);
                invokeAll(leftWorker, rightWorker);
                merge(left, mid, right); // merge the two sorted halves
            }
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


