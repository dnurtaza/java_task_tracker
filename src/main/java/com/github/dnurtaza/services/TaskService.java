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

    public TaskService(JsonStorage storage) {
        this.storage = storage;
    }

    public int getAIId(Map<Integer, Task> taskList)
    {
        return taskList.keySet().stream().max(Integer::compare).orElse(0) + 1;
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

        storage.save(JsonStorage.fromListOfObjectsToJson(new ArrayList<>(taskList.values())));

        return task;
    }

    public Task findTask(Map<Integer, Task> taskList, int id)
    {
        Task task = taskList.get(id);

        if (task == null) {
            throw new TaskNotFoundException(id);
        }

        return task;
    }

    public Task updateTaskById(int id, String desc) {
        Map<Integer, Task> taskList = getTaskList();

        Task task = findTask(taskList, id);

        task.setDescription(desc);

        String now = LocalDateTime.now().format(Task.FORMATTER);
        task.setUpdatedAt(now);

        storage.save(JsonStorage.fromListOfObjectsToJson(new ArrayList<>(taskList.values())));

        return task;
    }

    public Task updateTaskStatusById(int id, TaskStatus status)
    {
        Map<Integer, Task> taskList = getTaskList();

        Task task = findTask(taskList, id);

        task.setStatus(status);

        String now = LocalDateTime.now().format(Task.FORMATTER);
        task.setUpdatedAt(now);

        storage.save(JsonStorage.fromListOfObjectsToJson(new ArrayList<>(taskList.values())));

        return task;
    }

    public Task deleteTaskById(int id)
    {
        Map<Integer, Task> taskList = getTaskList();

        Task task = findTask(taskList, id);

        taskList.remove(id);

        storage.save(JsonStorage.fromListOfObjectsToJson(new ArrayList<>(taskList.values())));

        return task;
    }

    public void listAllTasks()
    {
        List<Task> tasks = new ArrayList<>(getTaskList().values());

        if (tasks.isEmpty()) {
            throw new NoTasksException();
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
            throw new NoTasksException();
        } else {
            tasks.stream()
                    .filter(task -> task.getStatus().equals(status))
                    .forEach(task -> System.out.println(
                            task.toString()
                    ));
        }
    }
}