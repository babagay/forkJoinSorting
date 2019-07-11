import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.RecursiveTask;

/**
 * Таска смотрит, есть ли файлы в папке ./pieces.
 *      Если нету, поднять исходный файл raw.csv и разбить на куски - заданного размера - (и сохранить их в pieces).
 *      В этом случае таска должна вернуть ...
 * Если есть доступные файлы в pieces, открыть доступный файл и отсортировать, после чего, удалить.
 *      Отсортированный файл поместить в ./sorted_pieces
 *          Либо перезаписать данный файл?
 *
 * TODO
 * Стартуем таску. В конструктор передали нужный размер куска
 * В методе compute():
 *      смотрим, есть ли файлы в папке ./pieces
 *      Если нет, делим исходный файл напополам. По завершении проверяем размер половинок, берем максимальный из двух и его возвращаем.
 *                Далее, код получивший этот результат сравнивает его с требуемым размером куска и принимает решение о запуске новой таски.
 *      Если да (есть готовые куски в папке pieces), перебираем их и проверяем размер. Если он больше заданного размера куска, производим дробление напополам.
 *              Возвращем наибольший размер из двух половинок.
 *              Код, получивший результат, запускает новую таску, если полученное число больше требуемого размера куска.
 * Т.к. тут не нужно возвращать результат, по сути, может, имеет смысл использовать RecursiveAction?
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
         * Также, можно ограничить размер неким числом (скажем, в 5 элементов или )
         */
        if (arrayToDivide.length > 1){

            // Разбить массив на 2 части
            List<int[]> partitionedArray = partitionArray();

            // Назначить новые таски для этих частей
            Task task1 = new Task(partitionedArray.get(0));
            Task task2 = new Task(partitionedArray.get(1));
            invokeAll(task1, task2);

            // Ожидаем результат обоих задач
            int[] array1 = task1.join();
            int[] array2 = task2.join();

            // Объявляем массив, в котором будет общий результат двух задач
            int[] mergedArray = new int[array1.length + array2.length];

            // Объединить результаты двух задач в один массив
            mergeArrays(array1, array2, mergedArray);
            // mergeArrays(task1.join(), task2.join(), mergedArray); // В примере использован такой код, почему-то

            return mergedArray;
        }

        return arrayToDivide;
    }

    private List<int[]> partitionArray(){

        int[] slice1 = Arrays.copyOfRange(arrayToDivide, 0, arrayToDivide.length/2);
        int[] slice2 = Arrays.copyOfRange(arrayToDivide, arrayToDivide.length/2, arrayToDivide.length);
        return Arrays.asList(slice1, slice2);
    }

    /**
     * Метод, специфичный для конкретной задачи (сортировка массива)
     */
    private void mergeArrays(
            int[] array1,
            int[] array2,
            int[] mergedArray) {

        int i = 0, j = 0, k = 0;

        while ((i < array1.length) && (j < array2.length)) {

            if (array1[i] < array2[j]) {
                mergedArray[k] = array1[i++];
            } else {
                mergedArray[k] = array2[j++];
            }

            k++;
        }

        if (i == array1.length) {

            for (int a = j; a < array2.length; a++) {
                mergedArray[k++] = array2[a];
            }

        } else {

            for (int a = i; a < array1.length; a++) {
                mergedArray[k++] = array1[a];
            }

        }
    }

}
