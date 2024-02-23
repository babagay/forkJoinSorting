package MergeSort;

import java.util.Arrays;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

/**
 * Альтернативная имплементация воркера,
 * когда задачи дробятся до момента, пока на входе не окажется массив с одним элементом
 * <p>
 * Заметил, что в такой реализации при размере массива в 100 элементов
 * все происходит в одном потоке
 * Т.е. форкДжоин сам принимает решение о необходимости дробления?
 * При размере в 10K появляется один воркер. И то, не всегда
 * <p>
 * Профилирование показывает, что при данной реализации
 * львиная доля времени тратится не на отработку кода метода merge(),
 * а на форканье и генерацию новых воркеров
 * В то время, как при линейной имплементации все уходит на метод merge.
 * И так же происходит в параллельной реализации V1.
 * <p>
 * Изначально я делал реализацию подобную этой,
 * но она чем-то отличалась и программа выполнялась крайне долго.
 * Я думал, не рационально продуцировать тучу воркеров... Однако, вышло наоборот
 *
 * Другой занимательный момент - массив sortedArray, по всей видимости, меняется одновременно
 * несколькими потоками
 * Справка: Массивы в Java не являются потокобезопасными структурами данных.
 * Поэтому изменение массива из нескольких потоков без синхронизации может привести к гонкам данных
 * и непредсказуемому поведению программы. В данном случае,
 * так как каждый поток обрабатывает разные участки массива без пересечения,
 * и запись результатов слияния происходит в разные индексы массива,
 * данный код работает корректно.
 * Однако это не является безопасным подходом для изменения общего состояния массива
 * в многопоточной среде.
 *
 * [Results]
 * 		Array size: 100M. Sequential sort: 36s. Parallel sort: 25s. Threshold: no
 * 		Array size: 100M. Sequential sort: 40s. Parallel sort: 27s. Threshold: yes (1K)
 * 		Array size: 100M. Sequential sort: 38s. Parallel sort: 22s. Threshold: yes (10K)
 * 		Array size: 100M. Sequential sort: 35s. Parallel sort: 22s. Threshold: yes (100K)
 * 		Array size: 100M. Sequential sort: 48s. Parallel sort: 25s. Threshold: yes (.1K). Profiler: enabled
 * 		Array size: 100M. Sequential sort: 48s. Parallel sort: 25s. Threshold: yes (10K). Profiler: enabled
 * 		Array size: 100M. Sequential sort: 52s. Parallel sort: 30s. Threshold: no. Profiler: enabled
 */
public class MergeSortParallelV2 {

    private final static int THRESHOLD = 10_000; // used in ForkJoin to figure out when should we switch to sequental calculations
    private final static boolean USE_THRESHOLD = false;

    private static long sequentialTime;
    private static long parallelTime;

    private long[] sortedArray;

    public MergeSortParallelV2(long[] sortedArray) {
        this.sortedArray = sortedArray;
    }

    public static void main(String[] args) {
        String arrayFile = "int.array100M.txt"; // Example: "int.array1m.txt"; // "int.array100.txt";

        long[] testArray = ArrayUtil.getArrayFromFile(arrayFile);
        System.out.println("Array get from " + arrayFile + ". Size: " + testArray.length);

        System.out.println("Start sequential sort........");
        MergeSortSequential mss = new MergeSortSequential(Arrays.copyOf(testArray, testArray.length));
        long startTime = System.currentTimeMillis();
        long[] sequentialResult = mss.doSorting();
        long endTime = System.currentTimeMillis();
        sequentialTime = endTime - startTime;
        System.out.println("Execution time (Sequential): " + sequentialTime / 1000.0 + "s");

        if (testArray.length <= 1000)
            System.out.println("Before sort: " + ArrayUtil.arrayToString(testArray));

        System.out.println("Start Parallel sort.......");
        if (USE_THRESHOLD) System.out.println("Threshold: yes (" + THRESHOLD + ")"); else System.out.println("Threshold: no");
        MergeSortParallelV2 sorter = new MergeSortParallelV2(Arrays.copyOf(testArray, testArray.length));
        startTime = System.currentTimeMillis();
        sorter.sort();
        endTime = System.currentTimeMillis();

        if (testArray.length <= 1000)
            System.out.println("After sort: " + ArrayUtil.arrayToString(sorter.sortedArray));

        parallelTime = endTime - startTime;
        System.out.println("Execution time (Parallel): " + parallelTime / 1000.0 + "s");

        System.out.println("Seq sort result equals parallel sort result: " + Arrays.equals(sorter.sortedArray, sequentialResult));

        float speedup = (float) sequentialTime / parallelTime;
        System.out.format("Speedup: %.2f \n", speedup);
        System.out.format("Efficiency: %.2f%%\n", (float) 100 * speedup / Runtime.getRuntime().availableProcessors());
    }

    void sort() {
        // setup custom pool
        int parallelism = Runtime.getRuntime().availableProcessors();
        ForkJoinPool forkJoinPool = new ForkJoinPool(parallelism);

        Worker task = new Worker(0, sortedArray.length - 1);
        forkJoinPool.invoke(task);
    }

    class Worker extends RecursiveAction {
        private final int left;
        private final int right;

        public Worker(int left, int right) {
            this.left = left;
            this.right = right;
        }

        @Override
        protected void compute() {
            if (right > left && (right - left) < THRESHOLD && USE_THRESHOLD){
                // [!] this section can be commented for experiment
                mergeSortSeq(left, right);
            } else
            if (left < right) {
                int mid = (left + right) / 2;
                Worker leftWorker = new Worker(left, mid);
                Worker rightWorker = new Worker(mid + 1, right);
                invokeAll(leftWorker, rightWorker); // there is fork under the hood
                merge(left, mid, right);
            }
        }

        void mergeSortSeq(int left, int right) {
            if (left >= right) return; // base case

            int mid = (left + right) / 2;

            mergeSortSeq(left, mid);
            mergeSortSeq(mid + 1,right);
            merge(left, mid, right);
        }

        private void merge(int leftBorder, int middleThreshold, int rightBorder) {
            int leftTempIndex = 0, rightTempIndex = 0, mergeIndex = leftBorder;

            long[] leftPart = Arrays.copyOfRange(sortedArray, leftBorder, middleThreshold + 1);
            long[] rightPart = Arrays.copyOfRange(sortedArray, middleThreshold + 1, rightBorder + 1);

            while (leftTempIndex < middleThreshold - leftBorder + 1 || rightTempIndex < rightBorder - middleThreshold) {
                if (leftTempIndex < middleThreshold - leftBorder + 1 && rightTempIndex < rightBorder - middleThreshold) {
                    if (leftPart[leftTempIndex] <= rightPart[rightTempIndex]) {
                        sortedArray[mergeIndex] = leftPart[leftTempIndex];
                        leftTempIndex++;
                    } else {
                        sortedArray[mergeIndex] = rightPart[rightTempIndex];
                        rightTempIndex++;
                    }
                } else if (leftTempIndex < middleThreshold - leftBorder + 1) {
                    sortedArray[mergeIndex] = leftPart[leftTempIndex];
                    leftTempIndex++;
                } else if (rightTempIndex < rightBorder - middleThreshold) {
                    sortedArray[mergeIndex] = rightPart[rightTempIndex];
                    rightTempIndex++;
                }
                mergeIndex++;
            }
        }
    }
}
