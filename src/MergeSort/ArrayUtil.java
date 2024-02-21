package MergeSort;

import java.io.*;
import java.util.Random;

public class ArrayUtil {

    //private static String ARR_SERIALIZED_FILE_NAME = "int.array.txt";
    //private static String ARR_SERIALIZED_FILE_NAME = "int.array1m.txt"; // over a million items
    private static String ARR_SERIALIZED_FILE_NAME = "int.array100M.txt"; // 100 items

    private static int ARR_SIZE = 100_000_000; // 1_000_000_000; долго. 1_000_000 - быстро. 10_000_000

    public static void main(String[] args) {
        // Генерация массива целых чисел
        long[] originalArray = generateArray(ARR_SIZE);

        // Сохранение массива в файл
        saveArrayToFile(originalArray, ARR_SERIALIZED_FILE_NAME);

        // Восстановление массива из файла
         long[] restoredArray = loadArrayFromFile(ARR_SERIALIZED_FILE_NAME);

        // Вывод результатов
        // System.out.println("Original Array: " + arrayToString(originalArray));
        // System.out.println("Restored Array: " + arrayToString(restoredArray));
    }

    private static long[] generateArray(int size) {
        long[] array = new long[size];
        for (int i = 0; i < size; i++) {
            // array[i] = i * 2; // Пример: заполняем массив четными числами
            array[i] = generateRandomNum(0, size * 2); // Пример: заполняем массив случайными числами
        }
        return array;
    }

    private static long generateRandomNum(int min, int max) {
        if (min > max) {
            throw new IllegalArgumentException("Min value must be less than or equal to max value");
        }

        // Инициализация объекта Random
        Random random = new Random();

        // Генерация случайного целого числа в заданном диапазоне
        // Формула: random.nextInt((max - min) + 1) + min
        return random.nextLong((max - min) + 1) + min;
    }

    private static void saveArrayToFile(long[] array, String fileName) {
        try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(fileName))) {
            outputStream.writeObject(array);
            System.out.println("Array saved to file: " + fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static long[] loadArrayFromFile(String fileName) {

        long[] array = null;
        try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(fileName))) {
            array = (long[]) inputStream.readObject();
            System.out.println("Array loaded from file: " + fileName);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return array;
    }

    public static String arrayToString(long[] array) {
        if (array == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < array.length; i++) {
            sb.append(array[i]);
            if (i < array.length - 1) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    public static long[] getArrayFromFile(String fileName) {
        if (fileName == null)
            fileName = ARR_SERIALIZED_FILE_NAME;

        long[] arr = loadArrayFromFile(fileName);

        return arr;
    }
}
