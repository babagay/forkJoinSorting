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
 */
public class MergeSortParallelV2 {

    private long[] sortedArray;

    public MergeSortParallelV2(long[] sortedArray) {
        this.sortedArray = sortedArray;
    }

    public static void main(String[] args) {
        String arrayFile = "int.array100M.txt"; // Example: "int.array1m.txt"; // "int.array100.txt";

        long[] testArray = ArrayUtil.getArrayFromFile(arrayFile);
        System.out.println("Array get from " + arrayFile + ". Size: " + testArray.length);

        if (testArray.length <= 1000)
            System.out.println("Before sort: " + ArrayUtil.arrayToString(testArray));

        MergeSortParallelV2 sorter = new MergeSortParallelV2(Arrays.copyOf(testArray, testArray.length));
        long startTime = System.currentTimeMillis();
        sorter.sort();
        long endTime = System.currentTimeMillis();

        if (testArray.length <= 1000)
            System.out.println("After sort: " + ArrayUtil.arrayToString(sorter.sortedArray));

        long executionTime = endTime - startTime;
        System.out.println("Execution time: " + executionTime / 1000.0 + "s");
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
            if (left < right) {
                int mid = (left + right) / 2;
                Worker leftWorker = new Worker(left, mid);
                Worker rightWorker = new Worker(mid + 1, right);
                invokeAll(leftWorker, rightWorker); // there is fork under the hood
                merge(left, mid, right);
            }
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
