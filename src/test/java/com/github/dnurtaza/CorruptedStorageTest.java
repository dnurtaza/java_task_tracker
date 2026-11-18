package com.github.dnurtaza;

import com.github.dnurtaza.models.TaskStatus;
import com.github.dnurtaza.services.TaskService;
import com.github.dnurtaza.storage.CorruptedStorageException;
import com.github.dnurtaza.storage.JsonParseException;
import com.github.dnurtaza.storage.JsonStorage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class CorruptedStorageTest
{
    @TempDir
    Path tempDir;

    private TaskService serviceWith(Path file, String content) throws IOException
    {
        Files.writeString(file, content);
        return new TaskService(new JsonStorage(file));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            // broken JSON syntax
            "not json at all",
            "[{\"id\": 1, \"description\": \"A\"}, \"status\": \"todo\"}]",
            "[{\"id\": 1, \"description\": \"A\", \"status\": \"todo\"",
            "[{\"id\": 1, \"description\": \"A\", \"status\": \"todo\"}] []",
            // valid JSON, but not valid tasks
            "[{\"id\": \"abc\", \"description\": \"A\", \"status\": \"todo\"}]",
            "[{\"description\": \"A\", \"status\": \"todo\"}]",
            "[{\"id\": 1, \"description\": \"A\", \"status\": \"finished\"}]",
            "[{\"id\": 1, \"description\": \"A\"}]",
            "[{\"id\": 1, \"status\": \"todo\"}]",
            "[{\"id\": 1, \"description\": \"A\", \"status\": \"todo\"}, {\"id\": 1, \"description\": \"B\", \"status\": \"done\"}]"
    })
    void corruptedFileIsReportedAndNeverOverwritten(String content) throws IOException
    {
        Path file = tempDir.resolve("tasks.json");
        TaskService service = serviceWith(file, content);

        assertThrows(CorruptedStorageException.class, service::getTaskList);
        assertThrows(CorruptedStorageException.class, () -> service.addTask("New task"));
        assertThrows(CorruptedStorageException.class, () -> service.updateTaskById(1, "Changed"));
        assertThrows(CorruptedStorageException.class, () -> service.updateTaskStatusById(1, TaskStatus.DONE));
        assertThrows(CorruptedStorageException.class, () -> service.deleteTaskById(1));
        assertThrows(CorruptedStorageException.class, service::listAllTasks);

        assertEquals(content, Files.readString(file), "Corrupted file must stay exactly as it was");
    }

    @Test
    void syntaxErrorKeepsJsonParseExceptionAsCause() throws IOException
    {
        TaskService service = serviceWith(tempDir.resolve("tasks.json"), "[{\"id\": 1,");

        CorruptedStorageException e = assertThrows(CorruptedStorageException.class, service::getTaskList);

        assertInstanceOf(JsonParseException.class, e.getCause());
    }

    @Test
    void messageNamesTheProblem() throws IOException
    {
        TaskService service = serviceWith(tempDir.resolve("tasks.json"),
                "[{\"id\": 7, \"description\": \"A\", \"status\": \"finished\"}]");

        CorruptedStorageException e = assertThrows(CorruptedStorageException.class, service::getTaskList);

        assertTrue(e.getMessage().contains("finished"), e.getMessage());
        assertTrue(e.getMessage().contains("7"), e.getMessage());
    }

    @Test
    void duplicateIdIsReported() throws IOException
    {
        TaskService service = serviceWith(tempDir.resolve("tasks.json"), """
                [{"id": 1, "description": "A", "status": "todo"},
                 {"id": 1, "description": "B", "status": "done"}]""");

        CorruptedStorageException e = assertThrows(CorruptedStorageException.class, service::getTaskList);

        assertTrue(e.getMessage().contains("Duplicate"), e.getMessage());
    }
}
