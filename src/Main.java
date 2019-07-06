import java.util.concurrent.ForkJoinPool;

public class Main {


    public static void main(String[] args) {

        // Или надо разбить на части файл и вызвать invokeAll?

        ForkJoinPool forkJoinPool = new ForkJoinPool();
        Task task = new Task();
        forkJoinPool.invoke(task);

    }
}
