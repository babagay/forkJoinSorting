// TODO перенести сюда все данные типа имени файла
class AppContext
{
    int nextSliceNumber(){
        return Context.INSTANCE.getSliceNumber();
    }

    int getSliceNumber(){
        return Context.INSTANCE.getSliceNumberVal();
    }

    void setTargetDir(String targetDir){
        Context.INSTANCE.setTargetDir(targetDir);
    }

    String getTargetDir(){
        return Context.INSTANCE.getTargetDir();
    }

    String getEncoding(){ // TODO
        return "UTF-8"; // Context.INSTANCE.getTargetDir();
    }

    void setEncoding(String targetDir){
        // TODO
    }

    String getFileName(){ // TODO
        return "log_medium"; // Context.INSTANCE.getTargetDir();
    }

    void setFileName(String targetDir){
        // TODO
    }

    String getFileExt(){ // TODO
        return "csv"; // Context.INSTANCE.getTargetDir();
    }

    void setFileExt(String targetDir){
        // TODO
    }

    /**
     * Максимальный объем доступной оперативной памяти
     */
    long getMemoryLimit(){ // TODO
        return 1024L; // Context.INSTANCE.getTargetDir();
    }

    void setMemoryLimit(long limit){
        // TODO
    }

    /**
     * Размер одной строки в байтах. Определяется эмпирически для конкретного csv-файла
     */
    int getLineCapacity(){ // TODO
        return 750; // Context.INSTANCE.getTargetDir();
    }

    void setLineCapacity(int capacity){
        // TODO
    }

    /**
     * Количество строк csv-файла, которые могут поместиться в памяти
     */
    long getLinesThreshold(){
        return getMemoryLimit() * 1024 * 1024 / getLineCapacity();
    }

}
