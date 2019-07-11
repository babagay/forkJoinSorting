import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

/**
 * https://github.com/babagay/lambda-hacking/blob/master/src/java8/exercises/Exercises.java
 * Чтение по байтам быстрее, но мы читаем по строкам, т.к. у нас на входе csv-файл и важна целостность данных:
 * одна строка - это одна запись в таблице.
 * Итак, по производительности:
 * java.nio.file.Files.readAllBytes() - самый быстрый
 * java.nio.file.Files.lines() - помедленнее
 * java.io.BufferedReader - еще немного хуже
 * <p>
 * Вариант с BufferedReader (A). Took 72,173 seconds to read to a 1682 MB file, rate: 24,4 MB/s
 * BufferedReader reader = Files.newBufferedReader(Paths.get(fileWithExt), StandardCharsets.UTF_8);
 * // Преобразовать файл в поток строк
 * reader.lines().forEach(System.out::println);
 * reader.close();
 * <p>
 * Пример чтения файла по строкам с использованием BufferedReader (B). 51,384 seconds to read to a 1682 MB file, rate: 34,3 MB/s
 * try {
 * BufferedReader reader = Files.newBufferedReader(Paths.get(fileWithExt));
 * Stream <String> lines = reader.lines();
 * lines.forEach(System.out::println);
 * lines.close();
 * } catch (IOException io) {
 * io.printStackTrace();
 * }
 * <p>
 * Пример чтения с использованием NIO Files. Took 51,458 seconds to read to a 1682 MB file, rate: 34,3 MB/s
 * try (Stream linesStream = Files.lines(file.toPath())) {
 * linesStream.forEach(System.out::println);
 * }
 * <p>
 * TODO
 * посмотреть синглтон ENUM
 * обернуть этот код в CompletableFuture
 * осуществить запись в файл также с помощью NIO
 * Здесь - http://qaru.site/questions/107445/read-large-files-in-java - про "Kickoff" (лаконичный пример реализации дробления)
 * Здесь - http://qaru.site/questions/40696/java-nio-filechannel-versus-fileoutputstream-performance-usefulness - про FileChannel
 * Все-таки, нам нужен RecursiveTask, чтобы он возвращал размер полученного файла и вышестоящий код мог приянть решение, надо ли форкать дальше или нет
 * И это решение принимается на основании сравнения размера файла с ограничением памяти
 */
public class FileDevider
{
    private String targetDir = "";
    private String encoding;
    private String fileName;
    private String fileExt;
    private String fileWithExt;
    private long MEMORY_LIMIT_MB;
    private int LINE_CAPACITY; // Размер одной строки в байтах. Определяется эмпирически для конкретного csv-файла
//    private long LINES_THRESHOLD; // Количество строк, которые могут поместиться в памяти

    FileDevider(AppContext registry)
    {
        targetDir = registry.getTargetDir();
        encoding = registry.getEncoding();
        fileName = registry.getFileName();
        fileExt = registry.getFileExt();
        fileWithExt = fileName + "." + fileExt;
        MEMORY_LIMIT_MB = registry.getMemoryLimit();
        LINE_CAPACITY = registry.getLineCapacity();
//        LINES_THRESHOLD = MEMORY_LIMIT_MB * 1024 * 1024 / LINE_CAPACITY;
    }

    public static void main(String[] args)
    {
        long start2 = System.nanoTime(); // Track time

        FileDevider divider = new FileDevider(null);

        divider.divide();

        // TODO перенести статистику в App
        // Вывод статистики
        // 64 << 2 = 64 * 2^2 = 64 * 4 = 256 - сдвиг влево
        // 64 >> 2 = 64 / 2^2 = 64 / 4 = 16 - сдвиг вправо
        long time2 = System.nanoTime() - start2;
        File file = new File(divider.fileWithExt);
        System.out.printf("Took %.3f seconds to read to a %d MB file, rate: %.1f MB/s%n",
                time2 / 1e9,
                file.length() >> 20,
                file.length() * 1000.0 / time2
        );
    }

    private int getCurrentPieceIndex()
    {
        return Context.INSTANCE.getSliceNumber();
    }

    public long divide()
    {
        File file = new File(fileWithExt);

        long fileSize = file.length();

        System.out.println("Старт деления файла");

        long linesCount = fileSize / LINE_CAPACITY; // Приблизительное количество строк в файле, который делим
        long halfLines = linesCount / 2 + 10; // Количество строк в половинке файла

        AtomicReference<BufferedWriter> writer = new AtomicReference<>(getWriter()); // Берем writer для первой половинки файла
        AtomicLong count = new AtomicLong(0L);

        try (Stream linesStream = Files.lines(file.toPath()))
        {
            linesStream.forEach(line -> {
                count.getAndIncrement();

                try
                {
                    if (count.get() % halfLines == 0)
                    {
                        // Достигнуто заданное число строк - пора отпускать полученный файл и начинать писать остаток в другую половинку
                        // TODO: как получить имя файла, чтобы узнать его размер?
                        System.out.println(count.get() + " lines written to file. " + writer.get().toString());
                        writer.get().close();
                        writer.set(getWriter());
                    }

                    writer.get().write(line.toString());
                    writer.get().newLine();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            });
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                System.out.println(count.get() + " lines written to file. " + writer.get().toString());
                writer.get().close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        return fileSize;
    }

    // TODO можно поиграть с размером writeBuffer
    private BufferedWriter getWriter()
    {
        BufferedWriter writer = null;
        File newDir = new File(targetDir);
        String pieceFileName = fileName + "_" + String.format("%02d", getCurrentPieceIndex()) + "." + fileExt;

        System.out.println("Запрошен writer для файла " + pieceFileName);

        Path splitFile = Paths.get(newDir.getPath() + "\\" + pieceFileName);
        try
        {
            writer = Files.newBufferedWriter(splitFile, StandardOpenOption.CREATE);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return writer;
    }
}
