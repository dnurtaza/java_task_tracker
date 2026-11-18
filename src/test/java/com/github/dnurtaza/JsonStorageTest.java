package com.github.dnurtaza;

import com.github.dnurtaza.models.Task;
import com.github.dnurtaza.models.TaskStatus;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.github.dnurtaza.storage.JsonStorage;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class JsonStorageTest
{
    @ParameterizedTest
    @MethodSource("escapeCases")
    void escapesSpecialCharacters(String input, String expected)
    {
        assertEquals(expected, JsonStorage.escape(input));
    }

    static Stream<Arguments> escapeCases()
    {
        return Stream.of(
                Arguments.of("Escapes \" quotes", "Escapes \\\" quotes"),
                Arguments.of("Escapes \\ back slashes", "Escapes \\\\ back slashes"),
                Arguments.of("Escapes \n new line", "Escapes \\n new line"),
                Arguments.of("Escapes \t horizontal tabs", "Escapes \\t horizontal tabs"),
                Arguments.of("Escapes \b backspaces", "Escapes \\b backspaces"),
                Arguments.of("Escapes \f new page", "Escapes \\f new page")
        );
    }

    @Test
    void convertsListOfTasksToJson()
    {
        List<Task> taskList = new ArrayList<>();

        taskList.add(new Task(1, "Do homework", TaskStatus.TO_DO, "16.11.2026 10:37", "16.11.2026 10:37"));
        taskList.add(new Task(2, "Buy food", TaskStatus.IN_PROGRESS, "16.11.2026 11:37", "16.11.2026 16:37"));
        taskList.add(new Task(3, "Go to gym", TaskStatus.TO_DO, "16.11.2026 12:37", "16.11.2026 12:37"));
        taskList.add(new Task(4, "Call mom", TaskStatus.DONE, "16.11.2026 13:37", "16.11.2026 15:37"));

        assertEquals(
                "[{\"id\": 1, \"description\": \"Do homework\", \"status\": \"todo\", \"createdAt\": \"16.11.2026 10:37\", \"updatedAt\": \"16.11.2026 10:37\"}, {\"id\": 2, \"description\": \"Buy food\", \"status\": \"in-progress\", \"createdAt\": \"16.11.2026 11:37\", \"updatedAt\": \"16.11.2026 16:37\"}, {\"id\": 3, \"description\": \"Go to gym\", \"status\": \"todo\", \"createdAt\": \"16.11.2026 12:37\", \"updatedAt\": \"16.11.2026 12:37\"}, {\"id\": 4, \"description\": \"Call mom\", \"status\": \"done\", \"createdAt\": \"16.11.2026 13:37\", \"updatedAt\": \"16.11.2026 15:37\"}]",
                JsonStorage.fromListOfTasksToJson(taskList)
        );
    }

    @Test
    void convertsFromTaskToJson()
    {
        Task task = new Task(1, "Do homework", TaskStatus.TO_DO, "16.11.2026 10:37", "16.11.2026 10:37");

        assertEquals(
                "{\"id\": 1, \"description\": \"Do homework\", \"status\": \"todo\", \"createdAt\": \"16.11.2026 10:37\", \"updatedAt\": \"16.11.2026 10:37\"}",
                JsonStorage.fromObjectToJson(task)
        );
    }

    @Test
    void convertingTaskListToJson()
    {
        Map<Integer, Task> taskList = new HashMap<>();

        taskList.put(1, new Task(1, "Do homework", TaskStatus.TO_DO, "16.11.2026 10:37", "16.11.2026 10:37"));
        taskList.put(2, new Task(2, "Buy food", TaskStatus.IN_PROGRESS, "16.11.2026 11:37", "16.11.2026 16:37"));
        taskList.put(3, new Task(3, "Go to gym", TaskStatus.TO_DO, "16.11.2026 12:37", "16.11.2026 12:37"));
        taskList.put(4, new Task(4, "Call mom", TaskStatus.DONE, "16.11.2026 13:37", "16.11.2026 15:37"));

        String expected = "[{\"id\": 1, \"description\": \"Do homework\", \"status\": \"todo\", \"createdAt\": \"16.11.2026 10:37\", \"updatedAt\": \"16.11.2026 10:37\"}, {\"id\": 2, \"description\": \"Buy food\", \"status\": \"in-progress\", \"createdAt\": \"16.11.2026 11:37\", \"updatedAt\": \"16.11.2026 16:37\"}, {\"id\": 3, \"description\": \"Go to gym\", \"status\": \"todo\", \"createdAt\": \"16.11.2026 12:37\", \"updatedAt\": \"16.11.2026 12:37\"}, {\"id\": 4, \"description\": \"Call mom\", \"status\": \"done\", \"createdAt\": \"16.11.2026 13:37\", \"updatedAt\": \"16.11.2026 15:37\"}]";

        assertEquals(expected, JsonStorage.fromListOfTasksToJson(new ArrayList<>(taskList.values())));
    }
}
