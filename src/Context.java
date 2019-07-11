import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Храним здесь состояние
 */
enum Context
{
    // [!] На каждое поле энума создается экземпляр объекта Context (т.к. вызывается конструктор),
    // поэтому, если нужен синглтон, поле д.б. одно единственное
    INSTANCE
    ;

    private static AtomicInteger sliceNumber;
    private static AtomicReference<String> targetDir;

    static {
        sliceNumber = new AtomicInteger(0);
        targetDir = new AtomicReference<>("");
    }

    Context()
    {
        System.out.println("Context created");
    }

    public int getSliceNumber()
    {
        return sliceNumber.incrementAndGet();
    }

    public int getSliceNumberVal()
    {
        return sliceNumber.get();
    }

    public String getTargetDir(){
        return targetDir.get();
    }

    public void setTargetDir(String dir){
        targetDir.set(dir);
    }


}
