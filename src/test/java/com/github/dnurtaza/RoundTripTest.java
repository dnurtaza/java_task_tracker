package com.github.dnurtaza;

import com.github.dnurtaza.models.Task;
import com.github.dnurtaza.services.TaskService;
import com.github.dnurtaza.storage.JsonStorage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class RoundTripTest
{
    @TempDir
    Path tempDir;

    private Path file;

    private TaskService newService()
    {
        if (file == null) file = tempDir.resolve("tasks.json");
        return new TaskService(new JsonStorage(file));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "He said \"hi\"",
            "C:\\Users\\Daniyal\\tasks.json",
            "Ends with a backslash \\",
            "Escaped quote \\\" inside",
            "\"",
            "\\",
            "Line one\nLine two",
            "Tab\tseparated\rand carriage return",
            "Control char \u0001 and \u001f",
            "JSON-like text: {\"id\": 99}, [1, 2]",
            "Купить молоко и хлеб",
            "Сдать проект 🚀",
            "Unicode escape text \\u0041 stays literal"
    })
    void descriptionSurvivesAddAndReload(String description)
    {
        Task added = newService().addTask(description);

        Task loaded = newService().getTaskList().get(added.getId());

        assertEquals(description, loaded.getDescription());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "He said \"hi\"",
            "C:\\Users\\Daniyal\\tasks.json",
            "Line one\nLine two"
    })
    void descriptionSurvivesUpdateAndReload(String description)
    {
        Task added = newService().addTask("Plain task");

        newService().updateTaskById(added.getId(), description);

        assertEquals(description, newService().getTaskList().get(added.getId()).getDescription());
    }

    @Test
    void trickyDescriptionDoesNotBreakOtherTasks()
    {
        newService().addTask("First");
        newService().addTask("Tricky \"}, {\"id\": 5, \"description\": \"injected\\");
        newService().addTask("Third");

        Map<Integer, Task> tasks = newService().getTaskList();

        assertEquals(3, tasks.size());
        assertEquals("First", tasks.get(1).getDescription());
        assertEquals("Tricky \"}, {\"id\": 5, \"description\": \"injected\\", tasks.get(2).getDescription());
        assertEquals("Third", tasks.get(3).getDescription());
    }

    @Test
    void manySaveLoadCyclesDoNotChangeDescription()
    {
        String description = "Quote \" and backslash \\ together";
        Task added = newService().addTask(description);

        for (int i = 0; i < 5; i++) {
            newService().addTask("Filler " + i);
        }

        assertEquals(description, newService().getTaskList().get(added.getId()).getDescription());
    }
}
