package metrics.utilities;

import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Logger;

public class FileWriterUtils {
    public static void flushAndCloseFW(FileWriter fileWriter, Logger logger, String className) {
        try {
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            logger.info("Error in " + className + " while flushing/closing fileWriter !!!");
        }
    }
}