package MergeSort;

import java.util.Arrays;
import java.util.Random;

/**
 * Main class
 * to compare sequential vs parallel implementations
 * of merge sort algorythm
 * provided by Concurrency course authors
 */
public class MergeSortV3App {
    /* helper function to generate array of random integers */
    public static int[] generateRandomArray(int length) {
        System.out.format("Generating random array int[%d]...\n", length);
        Random rand = new Random();
        int[] output = new int[length];
        for (int i=0; i<length; i++)
            output[i] = rand.nextInt();
        return output;
    }

    /* evaluate performance of sequential and parallel implementations */
    public static void main(String[] args) {
        final int NUM_EVAL_RUNS = 5;
        final int[] input = generateRandomArray(10_000_000); // 100_000_000 too much

        System.out.println("Evaluating Sequential Implementation...");
        SequentialMergeSorterV3 sms = new SequentialMergeSorterV3(Arrays.copyOf(input, input.length));
        int[] sequentialResult = sms.sort();
        double sequentialTime = 0;
        for(int i=0; i<NUM_EVAL_RUNS; i++) {
            System.out.println("Sequential sort, iteration: " + (i + 1));
            sms = new SequentialMergeSorterV3(Arrays.copyOf(input, input.length));
            long start = System.currentTimeMillis();
            sms.sort();
            sequentialTime += System.currentTimeMillis() - start;
        }
        sequentialTime /= NUM_EVAL_RUNS;

        System.out.println("Evaluating Parallel Implementation...");
        MergeSortParallelV3 pms = new MergeSortParallelV3(Arrays.copyOf(input, input.length));
        int[] parallelResult = pms.sort();
        double parallelTime = 0;
        for (int i = 0; i < NUM_EVAL_RUNS; i++) {
            System.out.println("Parallel sort: " + (i + 1));
            pms = new MergeSortParallelV3(Arrays.copyOf(input, input.length));
            long start = System.currentTimeMillis();
            pms.sort();
            parallelTime += System.currentTimeMillis() - start;
        }
        parallelTime /= NUM_EVAL_RUNS;

        // display sequential and parallel results for comparison
        if (!Arrays.equals(sequentialResult, parallelResult))
            throw new Error("ERROR: sequentialResult and parallelResult do not match!");
        System.out.format("Average Sequential Time: %.1f ms\n", sequentialTime);
        System.out.format("Average Parallel Time: %.1f ms\n", parallelTime);
        System.out.format("Speedup: %.2f \n", sequentialTime/parallelTime);
        System.out.format("Efficiency: %.2f%%\n", 100*(sequentialTime/parallelTime)/Runtime.getRuntime().availableProcessors());
    }
}
