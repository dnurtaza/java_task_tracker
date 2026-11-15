package com.github.dnurtaza.services;

import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.github.dnurtaza.models.Task;
import com.github.dnurtaza.models.TaskStatus;
import com.github.dnurtaza.storage.JsonStorage;

// [{"id": 1, "description": "Fix {bugs}", ...}, {"id": 1, "description": "Fix {bugs}", ...}]


public class TaskService
{
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
    private static int idCounter = 0;

    public static int getIdCounter()
    {
        return idCounter;
    }

    public static void initializeIdCounter()
    {
        Map<Integer, Task> taskList = getTaskList();

        if (taskList.isEmpty()) return;

        idCounter = Collections.max(taskList.keySet());
    }

    public static String fromTaskListToJson(Map<Integer, Task> taskList)
    {
        if (taskList.isEmpty()) return "[]";

        String json = "[";

        int i = 1;
        for (Task task : taskList.values()) {
            json += task.toJson() + ((i != taskList.size()) ? "," : "");
            i += 1;
        }

        json += "]";

        return json;
    }

    public static Map<Integer, Task> getTaskList()
    {
        Map<Integer, Task> taskList = new HashMap<>();

        String content = JsonStorage.readFromJson();

        if (content.equals("[]")) return taskList;

        String[] objects = content.substring(1, content.length() - 1).trim().split("\\}\\s*,\\s*\\{");

        outerLoop:
        for (String obj : objects) {
            obj = obj.trim();

            if (obj.startsWith("{")) obj = obj.substring(1);
            if (obj.endsWith("}")) obj = obj.substring(0, obj.length() - 1);

            String[] fields = obj.split("\\s*,\\s*\"");

            int id = 0;
            String desc = "";
            TaskStatus status = null;
            String createdAt = "";
            String updatedAt = "";

            String[] kv = new String[2];

            for (String field : fields) {
                kv = field.replace("\"", "").trim().split("\\s*:\\s*", 2);

                if (kv.length < 2) continue  outerLoop;

                String key = kv[0];
                String val = kv[1];

                switch (key) {
                    case "id" -> id = Integer.parseInt(val);
                    case "description" -> desc = val;
                    case "status" -> {
                        if (TaskStatus.fromTitle(val) == null) continue outerLoop;
                        status = TaskStatus.fromTitle(val);
                    }
                    case "createdAt" -> createdAt = val;
                    case "updatedAt" -> updatedAt = val;
                }
            }
            taskList.put(id, new Task(id, desc, status, createdAt, updatedAt));
        }

        return taskList;
    }

    public static void addTask(Task task)
    {
        String json = JsonStorage.readFromJson().trim();

        if (json == null || json.equals("[]") || json.isEmpty()) {
            JsonStorage.saveToJson("[" + task.toJson() + "]");
            return;
        }

        json = json.substring(0, json.lastIndexOf("]"));

        JsonStorage.saveToJson(json + "," + task.toJson() + "]");

        idCounter += 1;

        System.out.printf("Task \"%s\" was added successfully (ID: %d)%n", task.getDescription(), task.getId());
    }

    public static void updateTaskById(int id, String desc)
    {
        Map<Integer, Task> taskList = getTaskList();

        if (taskList.isEmpty() || !taskList.containsKey(id)) return;

        Task task = taskList.get(id);

        task.setDescription(desc);

        String now = LocalDateTime.now().format(FORMATTER);
        task.setUpdatedAt(now);

        JsonStorage.saveToJson(fromTaskListToJson(taskList));

        System.out.printf("Task description was updated successfully (ID: %d | Description: \"%s\")%n",
                id, task.getDescription());
    }

    public static void updateTaskStatusById(int id, TaskStatus status)
    {
        Map<Integer, Task> taskList = getTaskList();

        if (taskList.isEmpty() || !taskList.containsKey(id)) return;

        Task task = taskList.get(id);

        task.setStatus(status);

        String now = LocalDateTime.now().format(FORMATTER);
        task.setUpdatedAt(now);

        JsonStorage.saveToJson(fromTaskListToJson(taskList));

        System.out.printf("Status of the \"%s\" task was updated successfully (ID: %d | Status: \"%s\")%n",
                task.getDescription(), id, task.getStatus().getTitle());
    }

    public static void deleteTaskById(int id)
    {
        Map<Integer, Task> taskList = getTaskList();

        if (taskList.isEmpty() || !taskList.containsKey(id)) return;

        taskList.remove(id);

        JsonStorage.saveToJson(fromTaskListToJson(taskList));

        System.out.printf("The task was deleted successfully (ID: %d)%n", id);
    }

    public static void listAllTasks()
    {
        getTaskList().values().stream()
                .forEach(task -> System.out.println(
                        task.toString()
                ));
    }

    public static void listTasksByStatus(TaskStatus status)
    {
        Map<Integer, Task> taskList = getTaskList();

        if (taskList.isEmpty()) return;

        taskList.values()
                .stream()
                .filter(task -> task.getStatus().equals(status))
                .forEach(task -> System.out.println(
                    task.toString()
                ));
    }
}