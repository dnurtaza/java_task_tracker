package com.github.dnurtaza.storage;

import com.github.dnurtaza.models.Task;
import com.github.dnurtaza.models.TaskStatus;

import java.io.UncheckedIOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.nio.file.Files;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class JsonStorage {
    private final Path path;

    public JsonStorage(Path path) {
        this.path = path;
    }

    public void save(String json)
    {
        try {
            Files.writeString(this.path, json);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to write " + path, e);
        }
    }

    public String read()
    {
        if (!Files.exists(path)) {
            return "[]";
        }

        try {
            return Files.readString(path).trim();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read " + path, e);
        }
    }

    public static String fromListOfTasksToJson(List<Task> taskList)
    {
        return taskList.stream()
                .map(JsonStorage::fromObjectToJson)
                .collect(Collectors.joining(", ", "[", "]"));
    }

    public static String fromObjectToJson(Task task)
    {
        return "{"
                + "\"id\": " + task.getId() + ", "
                + "\"description\": \"" + escape(task.getDescription()) + "\", "
                + "\"status\": \"" + task.getStatus().getTitle() + "\", "
                + "\"createdAt\": \"" + escape(task.getCreatedAt()) + "\", "
                + "\"updatedAt\": \"" + escape(task.getUpdatedAt()) + "\""
                + "}";
    }

    public static String escape(String text)
    {
        StringBuilder sb = new StringBuilder();

        for (char c : text.toCharArray()) {
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                default -> {
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
                }
            }
        }

        return sb.toString();
    }
}