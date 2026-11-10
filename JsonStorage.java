import java.nio.file.Path;
import java.nio.file.Files;
import java.io.IOException;

import enums.TaskStatus;

class JsonStorage {
    private static final String FILE_NAME = "tasks.json";

    public static void saveToJson(String json)
    {
        try {
            Files.writeString(Path.of(FILE_NAME), json);
        } catch (IOException e) {
            System.err.println("Error, while trying to write: " + e.getMessage());
        }
    }

    public static String readFromJson()
    {
        Path path = Path.of(FILE_NAME);

        if (!Files.exists(path)) {
            return "[]";
        }

        try {
            return Files.readString(path).trim();
        } catch (IOException e) {
            System.err.println("Error, while trying to read: " + e.getMessage());
            return "[]";
        }
    }
}