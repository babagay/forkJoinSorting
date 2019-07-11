public class Registry
{
    private static Registry ourInstance = new Registry();

    public static Registry getInstance()
    {
        return ourInstance;
    }

    private Registry()
    {
    }
}
