package com.github.dnurtaza;

import com.github.dnurtaza.models.Task;
import com.github.dnurtaza.models.TaskStatus;
import com.github.dnurtaza.services.NoTasksException;
import com.github.dnurtaza.services.TaskNotFoundException;
import com.github.dnurtaza.services.TaskService;
import com.github.dnurtaza.storage.JsonStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class TaskServiceOperationsTest
{
    private static final String OLD_DATE = "16-11-2026 10:37:00";

    private static final String THREE_TASKS = """
            [{"id": 1, "description": "Do homework", "status": "todo", "createdAt": "16-11-2026 10:37:00", "updatedAt": "16-11-2026 10:37:00"},
            {"id": 2, "description": "Buy food", "status": "in-progress", "createdAt": "16-11-2026 11:37:00", "updatedAt": "16-11-2026 16:37:00"},
            {"id": 3, "description": "Call mom", "status": "done", "createdAt": "16-11-2026 13:37:00", "updatedAt": "16-11-2026 15:37:00"}]""";

    @TempDir
    Path tempDir;

    private Path file;
    private TaskService service;

    @BeforeEach
    void setUp() throws IOException
    {
        file = tempDir.resolve("tasks.json");
        Files.writeString(file, THREE_TASKS);
        service = new TaskService(new JsonStorage(file));
    }

    private Map<Integer, Task> reload()
    {
        return new TaskService(new JsonStorage(file)).getTaskList();
    }

    @Test
    void updateChangesDescriptionAndSavesIt()
    {
        Task returned = service.updateTaskById(1, "Do math homework");

        assertEquals("Do math homework", returned.getDescription());
        assertEquals("Do math homework", reload().get(1).getDescription());
    }

    @Test
    void updateRefreshesUpdatedAtButKeepsCreatedAtAndStatus()
    {
        service.updateTaskById(1, "Do math homework");
        Task task = reload().get(1);

        assertNotEquals(OLD_DATE, task.getUpdatedAt());
        assertEquals(OLD_DATE, task.getCreatedAt());
        assertEquals(TaskStatus.TO_DO, task.getStatus());
    }

    @Test
    void updateDoesNotTouchOtherTasks()
    {
        Map<Integer, Task> before = reload();

        service.updateTaskById(1, "Do math homework");
        Map<Integer, Task> after = reload();

        assertEquals(before.get(2).toString(), after.get(2).toString());
        assertEquals(before.get(3).toString(), after.get(3).toString());
    }

    @ParameterizedTest
    @EnumSource(TaskStatus.class)
    void updateStatusSavesNewStatus(TaskStatus status)
    {
        Task returned = service.updateTaskStatusById(1, status);

        assertEquals(status, returned.getStatus());
        assertEquals(status, reload().get(1).getStatus());
    }

    @Test
    void updateStatusRefreshesUpdatedAtButKeepsDescription()
    {
        service.updateTaskStatusById(1, TaskStatus.DONE);
        Task task = reload().get(1);

        assertNotEquals(OLD_DATE, task.getUpdatedAt());
        assertEquals(OLD_DATE, task.getCreatedAt());
        assertEquals("Do homework", task.getDescription());
    }

    @Test
    void deleteRemovesOnlyThatTask()
    {
        Task deleted = service.deleteTaskById(2);

        assertEquals(2, deleted.getId());
        assertEquals("Buy food", deleted.getDescription());
        assertEquals(List.of(1, 3), List.copyOf(reload().keySet()));
    }

    @Test
    void deletingEveryTaskLeavesEmptyArray() throws IOException
    {
        service.deleteTaskById(1);
        service.deleteTaskById(2);
        service.deleteTaskById(3);

        assertEquals("[]", Files.readString(file).trim());
        assertThrows(NoTasksException.class, () -> service.listAllTasks());
    }

    @Test
    void newIdAfterDeletingMiddleTaskIsStillMaxPlusOne()
    {
        service.deleteTaskById(2);
        Task added = service.addTask("Go to gym");

        assertEquals(4, added.getId());
    }

    @Test
    void updateUnknownIdThrows() throws IOException
    {
        TaskNotFoundException e = assertThrows(TaskNotFoundException.class,
                () -> service.updateTaskById(99, "Nothing"));

        assertEquals("Failed to find a task with ID: 99", e.getMessage());
        assertEquals(THREE_TASKS, Files.readString(file));
    }

    @Test
    void updateStatusUnknownIdThrows() throws IOException
    {
        assertThrows(TaskNotFoundException.class, () -> service.updateTaskStatusById(99, TaskStatus.DONE));
        assertEquals(THREE_TASKS, Files.readString(file));
    }

    @Test
    void deleteUnknownIdThrows() throws IOException
    {
        assertThrows(TaskNotFoundException.class, () -> service.deleteTaskById(99));
        assertEquals(THREE_TASKS, Files.readString(file));
    }

    @Test
    void deletingSameTaskTwiceThrowsTheSecondTime()
    {
        service.deleteTaskById(1);

        assertThrows(TaskNotFoundException.class, () -> service.deleteTaskById(1));
    }

    @Test
    void unknownIdWhenFileDoesNotExistThrowsAndCreatesNoFile()
    {
        Path missing = tempDir.resolve("missing.json");
        TaskService emptyService = new TaskService(new JsonStorage(missing));

        assertThrows(TaskNotFoundException.class, () -> emptyService.updateTaskById(1, "Nothing"));
        assertFalse(Files.exists(missing));
    }

    @Test
    void listAllReturnsTasksSortedById()
    {
        List<Integer> ids = service.listAllTasks().stream().map(Task::getId).toList();

        assertEquals(List.of(1, 2, 3), ids);
    }

    @Test
    void listByStatusReturnsOnlyMatchingTasks()
    {
        service.updateTaskStatusById(3, TaskStatus.TO_DO);

        List<Integer> ids = service.listTasksByStatus(TaskStatus.TO_DO).stream().map(Task::getId).toList();

        assertEquals(List.of(1, 3), ids);
    }

    @Test
    void listByStatusWithNoMatchesReturnsEmptyList()
    {
        service.deleteTaskById(3);

        assertThrows(NoTasksException.class, () -> service.listTasksByStatus(TaskStatus.DONE));
    }

    @Test
    void listOnEmptyStorageThrowsNoTasks()
    {
        TaskService emptyService = new TaskService(new JsonStorage(tempDir.resolve("missing.json")));

        assertThrows(NoTasksException.class, emptyService::listAllTasks);
        assertThrows(NoTasksException.class, () -> emptyService.listTasksByStatus(TaskStatus.TO_DO));
    }
}
