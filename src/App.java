/**
 * The basic application entry point
 */
public class App
{
    private AppContext context;

    public static void main(String[] args)
    {
        App app = new App();

        FileDevider divider = new FileDevider(app.context);

        divider.divide();
    }

    private App()
    {
        context = initContext();
    }

    /**
     * Load initial data to work
     */
    private AppContext initContext()
    {
        AppContext context = new AppContext();
        context.setTargetDir("pieces");
        context.setFileName("log_medium");
        context.setFileExt("csv");
        context.setMemoryLimit(1024); // 1 GB
        context.setLineCapacity(750); // Size of single row, bytes. Determined empirically for specific csv file

        return context;
    }
}
