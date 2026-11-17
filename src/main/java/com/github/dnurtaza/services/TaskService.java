package com.github.dnurtaza.services;

import java.util.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.github.dnurtaza.models.Task;
import com.github.dnurtaza.models.TaskStatus;
import com.github.dnurtaza.storage.JsonParser;
import com.github.dnurtaza.storage.JsonStorage;

public class TaskService
{
    private final JsonStorage storage;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
    private static int idCounter = 1;

    public TaskService(JsonStorage storage) {
        this.storage = storage;
    }

    public int getIdCounter()
    {
        return idCounter;
    }

    public void initializeIdCounter()
    {
        Map<Integer, Task> taskList = getTaskList();

        if (taskList.isEmpty()) return;

        idCounter = Collections.max(taskList.keySet());
    }

    public static String fromTaskListToJson(Map<Integer, Task> taskList)
    {
        if (taskList.isEmpty()) return "[]";

        StringBuilder json = new StringBuilder("[");

        int i = 1;
        for (Task task : taskList.values()) {
            json.append(task.toJson());
            if (i != taskList.size()) json.append(',');

            i += 1;
        }

        json.append(']');

        return json.toString();
    }

    public Map<Integer, Task> getTaskList()
    {
        Map<Integer, Task> taskList = new TreeMap<>();

        List<Map<String, String>> objects;
        try {
            objects = JsonParser.parseArray(storage.read());
        } catch (IllegalArgumentException e) {
            System.err.println("tasks.json is corrupted " + e.getMessage());
            return taskList;
        }

        for (Map<String, String> obj : objects) {
            int id;
            try {
                id = Integer.parseInt(obj.get("id"));
            } catch (NumberFormatException e) {
                System.err.println("Skipping task with invalid id: " + obj.get("id"));
                continue;
            }

            TaskStatus status = TaskStatus.fromTitle(obj.get("status"));
            if (status == null) {
                System.err.println("Skipping task with invalid status: '" + obj.get("status") + "'");
                continue;
            }

            taskList.put(id,
                    new Task(id, obj.get("description"), status, obj.getOrDefault("createdAt", ""), obj.getOrDefault("updatedAt", ""))
            );
        }

        return taskList;
    }

    public void addTask(Task task)
    {
        String json = storage.read().trim();

        if (json.equals("[]") || json.isEmpty()) {
            storage.save("[" + task.toJson() + "]");
            return;
        }

        json = json.substring(0, json.lastIndexOf("]"));

        storage.save(json + "," + task.toJson() + "]");

        idCounter += 1;

        System.out.printf("Task \"%s\" was added successfully (ID: %d)%n", task.getDescription(), task.getId());
    }

    public void updateTaskById(int id, String desc)
    {
        Map<Integer, Task> taskList = getTaskList();

        if (taskList.isEmpty() || !taskList.containsKey(id)) return;

        Task task = taskList.get(id);

        task.setDescription(desc);

        String now = LocalDateTime.now().format(FORMATTER);
        task.setUpdatedAt(now);

        storage.save(fromTaskListToJson(taskList));

        System.out.printf("Task description was updated successfully (ID: %d | Description: \"%s\")%n",
                id, task.getDescription());
    }

    public void updateTaskStatusById(int id, TaskStatus status)
    {
        Map<Integer, Task> taskList = getTaskList();

        if (taskList.isEmpty() || !taskList.containsKey(id)) return;

        Task task = taskList.get(id);

        task.setStatus(status);

        String now = LocalDateTime.now().format(FORMATTER);
        task.setUpdatedAt(now);

        storage.save(fromTaskListToJson(taskList));

        System.out.printf("Status of the \"%s\" task was updated successfully (ID: %d | Status: \"%s\")%n",
                task.getDescription(), id, task.getStatus().getTitle());
    }

    public  void deleteTaskById(int id)
    {
        Map<Integer, Task> taskList = getTaskList();

        if (taskList.isEmpty() || !taskList.containsKey(id)) return;

        taskList.remove(id);

        storage.save(fromTaskListToJson(taskList));

        System.out.printf("The task was deleted successfully (ID: %d)%n", id);
    }

    public void listAllTasks()
    {
        getTaskList().values()
                .forEach(task -> System.out.println(
                        task.toString()
                ));
    }

    public void listTasksByStatus(TaskStatus status)
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