import com.github.dnurtaza.models.Task;
import com.github.dnurtaza.models.TaskStatus;
import com.github.dnurtaza.services.TaskService;
import com.github.dnurtaza.storage.JsonStorage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class TaskServiceTest {
    @Test
    void convertingTaskListToJson()
    {
        Map<Integer, Task> taskList = new HashMap<>();

        taskList.put(1, new Task(1, "Do homework", TaskStatus.TO_DO, "16.11.2026 10:37", "16.11.2026 10:37"));
        taskList.put(2, new Task(2, "Buy food", TaskStatus.IN_PROGRESS, "16.11.2026 11:37", "16.11.2026 16:37"));
        taskList.put(3, new Task(3, "Go to gym", TaskStatus.TO_DO, "16.11.2026 12:37", "16.11.2026 12:37"));
        taskList.put(4, new Task(4, "Call mom", TaskStatus.DONE, "16.11.2026 13:37", "16.11.2026 15:37"));

        String expected = "[{\"id\": 1, \"description\": \"Do homework\", \"status\": \"todo\", \"createdAt\": \"16.11.2026 10:37\", \"updatedAt\": \"16.11.2026 10:37\"}, {\"id\": 2, \"description\": \"Buy food\", \"status\": \"in-progress\", \"createdAt\": \"16.11.2026 11:37\", \"updatedAt\": \"16.11.2026 16:37\"}, {\"id\": 3, \"description\": \"Go to gym\", \"status\": \"todo\", \"createdAt\": \"16.11.2026 12:37\", \"updatedAt\": \"16.11.2026 12:37\"}, {\"id\": 4, \"description\": \"Call mom\", \"status\": \"done\", \"createdAt\": \"16.11.2026 13:37\", \"updatedAt\": \"16.11.2026 15:37\"}]";

        assertEquals(expected, JsonStorage.fromListOfObjectsToJson(new ArrayList<>(taskList.values())));
    }

    @TempDir
    Path tempDir;

    @Test
    void readsTasksFromFile() throws IOException
    {
        Path file = tempDir.resolve("tasks.json");

        Files.writeString(file, """
                [{"id": 1, "description": "Do homework", "status": "todo", "createdAt": "16.11.2026 10:37", "updatedAt": "16.11.2026 10:37"},
                {"id": 2, "description": "Buy food", "status": "in-progress", "createdAt": "16.11.2026 11:37", "updatedAt": "16.11.2026 16:37"},
                {"id": 3, "description": "Go to gym", "status": "todo", "createdAt": "16.11.2026 12:37", "updatedAt": "16.11.2026 12:37"},
                {"id": 4, "description": "Call mom", "status": "done", "createdAt": "16.11.2026 13:37", "updatedAt": "16.11.2026 15:37"}]
                """);

        TaskService service = new TaskService(new JsonStorage(file));

        assertEquals(4, service.getTaskList().size());
        assertEquals("Go to gym", service.getTaskList().get(3).getDescription());
        assertEquals(TaskStatus.IN_PROGRESS, service.getTaskList().get(2).getStatus());
        assertEquals("16.11.2026 15:37", service.getTaskList().get(4).getUpdatedAt());
    }

    @Test
    void idAutoIncrement() throws IOException
    {
        Path file = tempDir.resolve("tasks.json");

        Files.writeString(file, "[]");

        TaskService service = new TaskService(new JsonStorage(file));
        service.addTask("Do homework");
        service.addTask("Buy food");

        Map<Integer, Task> taskList = service.getTaskList();

        assertEquals(List.of(1, 2), List.copyOf(taskList.keySet()));
    }

    @Test
    void idContinuesAfterHighestExistingId() throws IOException
    {
        Path file = tempDir.resolve("tasks.json");

        Files.writeString(file, """
                [{"id": 1, "description": "Do homework", "status": "todo", "createdAt": "16.11.2026 10:37", "updatedAt": "16.11.2026 10:37"},
                {"id": 2, "description": "Buy food", "status": "in-progress", "createdAt": "16.11.2026 11:37", "updatedAt": "16.11.2026 16:37"},
                {"id": 3, "description": "Go to gym", "status": "todo", "createdAt": "16.11.2026 12:37", "updatedAt": "16.11.2026 12:37"},
                {"id": 10, "description": "Call mom", "status": "done", "createdAt": "16.11.2026 13:37", "updatedAt": "16.11.2026 15:37"}]
                """);

        TaskService service = new TaskService(new JsonStorage(file));
        service.addTask("Meet Asset");
        service.addTask("Pick up Aruzhka");

        assertEquals(List.of(11, 12), List.copyOf(service.getTaskList().keySet()).subList(4, 6));
    }

    @Test
    void idsContinueAcrossProgramRuns() throws IOException
    {
        Path file = tempDir.resolve("tasks.json");

        new TaskService(new JsonStorage(file)).addTask("Do homework");
        Task task = new TaskService(new JsonStorage(file)).addTask("Buy food");

        assertEquals(2, task.getId());
    }
}
