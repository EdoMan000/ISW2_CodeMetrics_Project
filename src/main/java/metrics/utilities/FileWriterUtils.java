package metrics.utilities;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

public class FileWriterUtils {
    private FileWriterUtils() {
    }

    public static void flushAndCloseFW(FileWriter fileWriter, Logger logger, String className) {
        try {
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            logger.info("Error in " + className + " while flushing/closing fileWriter !!!");
        }
    }

    public static void deleteDirectory(String directoryPath) throws IOException {
        File directory = new File(directoryPath);
        if(directory.isDirectory()){
            File[] contents = directory.listFiles();
            if(contents!=null){
                for(File content : contents){
                    deleteDirectory(content.getAbsolutePath());
                }
            }
        }
        Files.delete(Path.of(directory.toURI()));
    }
}