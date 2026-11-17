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

    public TaskService(JsonStorage storage) {
        this.storage = storage;
    }

    public int getAIId(Map<Integer, Task> taskList)
    {
        return taskList.keySet().stream().max(Integer::compare).orElse(0) + 1;
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

        List<Map<String, String>> objects = JsonParser.parseArray(storage.read());


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

    public Task addTask(String description)
    {
        Map<Integer, Task> taskList = getTaskList();
        int id = getAIId(taskList);

        Task task = new Task(id, description);
        taskList.put(id, task);

        storage.save(fromTaskListToJson(taskList));

        System.out.printf("Task \"%s\" was added successfully (ID: %d)%n", task.getDescription(), task.getId());

        return task;
    }

    public void updateTaskById(int id, String desc)
    {
        Map<Integer, Task> taskList = getTaskList();

        if (taskList.isEmpty() || !taskList.containsKey(id)) {
            System.err.println("Failed to find a task with ID: " + id);
            return;
        }

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

        if (taskList.isEmpty() || !taskList.containsKey(id)) {
            System.err.println("Failed to find a task with ID: " + id);
            return;
        }

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

        if (taskList.isEmpty() || !taskList.containsKey(id)) {
            System.err.println("Failed to find a task with ID: " + id);
            return;
        }

        taskList.remove(id);

        storage.save(fromTaskListToJson(taskList));

        System.out.printf("The task was deleted successfully (ID: %d)%n", id);
    }

    public void listAllTasks()
    {
        List<Task> tasks = new ArrayList<>(getTaskList().values());

        if (tasks.isEmpty()) {
            System.out.println("There is no tasks yet.");
        } else {
            tasks.forEach(task -> System.out.println(
                    task.toString()
            ));
        }
    }

    public void listTasksByStatus(TaskStatus status)
    {
        List<Task> tasks = new ArrayList<>(getTaskList().values());

        if (tasks.isEmpty()) {
            System.out.println("There is no tasks yet.");
        } else {
            tasks.stream()
                    .filter(task -> task.getStatus().equals(status))
                    .forEach(task -> System.out.println(
                            task.toString()
                    ));
        }
    }
}