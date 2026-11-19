package com.github.dnurtaza.storage;

import com.github.dnurtaza.models.Task;
import com.github.dnurtaza.models.TaskStatus;

import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.Files;
import java.io.IOException;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class JsonStorage {
    private final Path path;

    public JsonStorage(Path path) {
        this.path = path;
    }

    public void save(Map<Integer, Task> taskList)
    {
        String json = fromListOfTasksToJson(taskList.values().stream().toList());
        try {
            Path tmp = path.resolveSibling(path.getFileName() + ".tmp");
            Files.writeString(tmp, json);
            Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
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

    public Map<Integer, Task> load()
    {
        List<Map<String, String>> objects;
        try {
            objects = JsonParser.parseArray(read());
        } catch (JsonParseException e) {
            throw new CorruptedStorageException("Invalid JSON: " + e.getMessage(), e);
        }

        Map<Integer, Task> taskList = new TreeMap<>();

        for (Map<String, String> obj : objects) {
            int id;
            try {
                id = Integer.parseInt(obj.get("id"));
            } catch (NumberFormatException e) {
                throw new CorruptedStorageException("Invalid id: '" + obj.get("id") + "'", e);
            }

            if (taskList.containsKey(id)) {
                throw new CorruptedStorageException("Duplicate id: " + id);
            }

            TaskStatus status = TaskStatus.fromTitle(obj.get("status"));
            if (status == null) {
                throw new CorruptedStorageException("Invalid status '" + obj.get("status") + "' in task " + id);
            }

            String description = obj.get("description");
            if (description == null) {
                throw new CorruptedStorageException("Missing description in task " + id);
            }

            taskList.put(id, new Task(id, description, status,
                    obj.getOrDefault("createdAt", ""), obj.getOrDefault("updatedAt", "")));
        }

        return taskList;
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