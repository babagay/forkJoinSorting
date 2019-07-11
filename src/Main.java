import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;


public class Main {


    public static void main(String[] args) {

        // Тестовый пример с использованием ForkJoinPool для сортировки массива

        int[] arrToSort = new int[]{10,2,4,45,1,10,78,6,22,15};

         // ExecutorService _forkJoinPool = Executors.newWorkStealingPool(); // Еще вариант получить ForkJoinPool

        ForkJoinPool forkJoinPool = new ForkJoinPool();
        Task task = new Task(arrToSort);
        int[] result = forkJoinPool.invoke(task);

        Arrays.stream(result).forEach(System.out::println);

        System.out.println("Sorted " + Arrays.toString(task.join()));
    }
}
