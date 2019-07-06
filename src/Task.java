import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveTask;

/**
 * Таска смотрит, есть ли файлы в папке ./pieces.
 *      Если нету, поднять файл raw.csv и разбить на куски - заданного размера - (и сохранить их в pieces).
 *      В этом случае таска должна вернуть ...
 * Если есть доступные файлы в pieces, открыть доступный файл и отсортировать, после чего, удалить.
 *      Отсортированный файл поместить в ./sorted_pieces
 *          Либо перезаписать данный файл?
 *
 *          https://javabeat.net/merge-sort-implemented-using-fork-join/
 *          https://hackernoon.com/parallel-merge-sort-in-java-e3213ae9fa2c
 */
public class Task extends RecursiveTask<int[]>
{
    int[] arrayToDivide;

    public Task()
    {

    }

    public Task(int[] arr)
    {

        arrayToDivide = arr;
    }

    @Override
    protected int[] compute()
    {
        List<RecursiveTask> forkedTasks = new ArrayList<>();

        /**
         * Делим массив на кусочки, пока не достигнем размера в 1 элемент.
         * Также, можно ограничить размер неким числом (скажем, в 5 элементов или 8192)
         * При достижении порога
         */
        if (arrayToDivide.length > 1){

        }

        return new int[0];
    }
}
