package MergeSort;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

/**
 * Сперва была испытана такая имплементация ForkJoin: задачи дробятся до массива единичной длины.
 * Программа исполняется 100 лет.
 * <p>
 * Нам не нужно использовать потокобезопасную структуру для хранения сортируемого массива.
 * Это обычный массив long[], как и был в классе последовательной сортировки
 * <p>
 * Моя имплементация параллельного merge sort
 * Исходный массив делится на 8 частей и каждая сортируется в отдельном потоке
 * Затем они сливаются
 * <p>
 * Результаты:
 * 8 Threads
 * 9 min - 10m, Arr size: 10m, THRESHOLD: 20 000
 * 18 sec - 10m, Arr size: 10m, THRESHOLD: 1 200 000
 * 26 sec - 10m, Arr size: 10m, THRESHOLD: 1 249 998
 * 5 sec - 10m, Arr size: 10m, THRESHOLD: 2 500 000
 * 3 sec - 10m, Arr size: 10m, THRESHOLD: 5 000 000
 * 2 sec - 10m, Arr size: 10m, THRESHOLD: 50 000 000 - single thread - MAX speed
 *
 * Результаты после изменения кода:
 * leftTask.fork(); 	rightTask.compute();   leftTask.join();
 * 8 sec - 10m, Arr size: 10m, THRESHOLD: 1 250 000 - 6 потоков
 * 8 sec - 10m, Arr size: 10m, THRESHOLD: 2 250 000 - 4 потоков
 * 13 sec - 10m, Arr size: 10m, THRESHOLD: 1 249 998 - 8 потоков
 *
 * Тенденция показывает, что эффективность максимальная при одном потоке!
 */
public class MergeSortParallel {

    private static int THRESHOLD;
    private final long[] testArray;

    public MergeSortParallel(String fileName) {
        testArray = ArrayUtil.getArrayFromFile(fileName);

        int parallelism = Runtime.getRuntime().availableProcessors();
        THRESHOLD = 2_249_998;
        //THRESHOLD = (testArray.length / parallelism);

        // System.out.println("Initial: " + ArrayUtil.arrayToString(testArray));
        System.out.println("THRESHOLD: " + THRESHOLD);
        System.out.println("Arr size : " + testArray.length);
    }

    public static void main(String[] args) {
        System.out.println("MergeSort Parallel.   "); // ENTRY POINT

        MergeSortParallel job = new MergeSortParallel("int.array1m.txt"); // "int.array.txt"; "int.array1m.txt" int.array100.txt
        long startTime = System.currentTimeMillis();

        // List<Long> sortedList = job.mergeSort();
        job.startSorting();

        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;
        System.out.println("Execution time: " + executionTime / 1000.0 + "s");

        // System.out.println("Sorted: " + ArrayUtil.arrayToString(job.testArray));
        // sortedList.forEach(System.out::println);
        // System.out.println("Sorted: " + sortedList);
    }

    void startSorting() { // START SORTING
        // преобразовывать в список не обязательно
//        CopyOnWriteArrayList<Long> list = Arrays.stream(testArray)
//                .boxed() // Преобразование long в Long
//                .parallel()
//                .collect(Collectors.toCollection(CopyOnWriteArrayList::new));

        SortTask task = new SortTask(testArray, 0, testArray.length);
        getPool().invoke(task);
    }

    ForkJoinPool getPool() {
        // create custom pool
        int parallelism = Runtime.getRuntime().availableProcessors();
        System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", String.valueOf(parallelism));
        ForkJoinPool forkJoinPool = new ForkJoinPool(parallelism);

        return forkJoinPool;

        // return ForkJoinPool.commonPool(); // use common pool
    }

    /**
     * Worker
     */
    private static class SortTask extends RecursiveTask<Void> {

        // private   List<Long> list;
        private final long[] sortingArray;
        private final int start;
        private final int end;

//        public SortTask(List<Long> list1) {
//            list = list1;
//        }

        public SortTask(long[] sortingArray, int startPosition, int endPosition) {
            this.sortingArray = sortingArray;
            this.start = startPosition;
            this.end = endPosition;
        }

        static void mergeSortSeq(long[] arr) {
            if (arr.length <= 1) return; // base case

            int mid = arr.length / 2;

            // divide array for two halfes
            long[] left = Arrays.copyOfRange(arr, 0, mid);
            long[] right = Arrays.copyOfRange(arr, mid, arr.length);

            mergeSortSeq(left);
            mergeSortSeq(right);
            mergeSeq(left, right, arr);
        }

        public static void mergeSeq(long[] left, long[] right, long[] result) {

            int l = 0, r = 0, i = 0;
            int leftBorder = left.length;
            int rightBorder = right.length;

            while (l < leftBorder && r < rightBorder) {
                if (left[l] < right[r]) {
                    result[i] = left[l];
                    l++;
                    i++;
                } else {
                    result[i] = right[r];
                    r++;
                    i++;
                }
            }

            while (l < leftBorder) {
                result[i] = left[l];
                l++;
                i++;
            }

            while (r < rightBorder) {
                result[i] = right[r];
                r++;
                i++;
            }
        }

        @Override
        protected Void compute() {
            // System.out.println("compute() - I am work in thread: " + Thread.currentThread().getName());

            int length = end - start;
            if (length <= THRESHOLD) {
                // Base case - switch to sequentional sorting and just do the job
                System.out.println("Base case triggered. length: " + length);

                // long[] result = Arrays.copyOfRange(sortingArray, start, end);

//                long[] longArray = list.stream()
//                        .mapToLong(Long::longValue)
//                        .toArray();

                // Call sequential sorting method

                mergeSortSeq(sortingArray); // OK

                // MergeSortSequential.mergeSort(sortingArray); // OK

                // System.out.println("BASE: " + Thread.currentThread().getName());

//                return Arrays.stream(result)
//                        .boxed()
//                        .collect(Collectors.toList());
            } else {
                // We can divide work. Then, fork

                // int mid = list.size() / 2;
                System.out.println("Fork");

                int mid = (start + end) / 2;

                // System.out.println("FORK: " + Thread.currentThread().getName());

                // divide array for two halves
                //long[] left = Arrays.copyOfRange(arr, 0, mid);
                //long[] right = Arrays.copyOfRange(arr, mid, arr.length);

                //CopyOnWriteArrayList<Long> left = new CopyOnWriteArrayList<Long>(list.subList(0, mid));
                //CopyOnWriteArrayList<Long> right = new CopyOnWriteArrayList<Long>(list.subList(mid, list.size()));
                // ArrayList<Long> left = new ArrayList<>(list.subList(0, mid));
                // ArrayList<Long> right = new ArrayList<>(list.subList(mid, list.size()));

                // [!] задача этого куска - разбить первичную таску на подзадачи. Всё
                // Объединять результат их работы не надо

                SortTask leftTask = new SortTask(sortingArray, start, mid);
                SortTask rightTask = new SortTask(sortingArray, mid, end);

                leftTask.fork();

                rightTask.compute();
                // rightTask.fork();

                leftTask.join();  // List<Long> u = leftTask.join();
                // rightTask.join(); // List<Long> v = rightTask.join();

                // merge(u, v, new ArrayList<>()); // merge(u, v, list);
            }

            return null;
        }

        /**
         * For parallel sorting
         */
        List<Long> merge(List<Long> left, List<Long> right, List<Long> result) {
            int l = 0, r = 0, i = 0;
            int leftBorder = left.size();
            int rightBorder = right.size();

            while (l < leftBorder && r < rightBorder) {
                if (left.get(l) < right.get(r)) {
                    result.add(i, left.get(l)); // result.set(i, left.get(l)); // set() - replace element
                    l++;
                    i++;
                } else {
                    result.add(i, right.get(r)); // result.set(i, right.get(r));
                    r++;
                    i++;
                }
            }

            while (l < leftBorder) {
                result.add(i, left.get(l)); // result.set(i,left.get(l));
                l++;
                i++;
            }

            while (r < rightBorder) {
                result.add(i, right.get(r)); // result.set(i,right.get(r));
                r++;
                i++;
            }

            return result;
        }


    }
}
