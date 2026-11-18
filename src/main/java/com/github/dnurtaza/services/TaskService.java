package com.github.dnurtaza.services;

import java.util.*;

import java.time.LocalDateTime;

import com.github.dnurtaza.models.Task;
import com.github.dnurtaza.models.TaskStatus;
import com.github.dnurtaza.storage.CorruptedStorageException;
import com.github.dnurtaza.storage.JsonParseException;
import com.github.dnurtaza.storage.JsonParser;
import com.github.dnurtaza.storage.JsonStorage;

public class TaskService
{
    private final JsonStorage storage;

    public TaskService(JsonStorage storage) {
        this.storage = storage;
    }

    private int nextId(Map<Integer, Task> taskList)
    {
        return taskList.keySet().stream().max(Integer::compare).orElse(0) + 1;
    }

    public Map<Integer, Task> getTaskList()
    {
        List<Map<String, String>> objects;
        try {
            objects = JsonParser.parseArray(storage.read());
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

    public Task addTask(String description)
    {
        Map<Integer, Task> taskList = getTaskList();
        int id = nextId(taskList);

        Task task = new Task(id, description);
        taskList.put(id, task);

        storage.save(JsonStorage.fromListOfTasksToJson(new ArrayList<>(taskList.values())));

        return task;
    }

    private Task findTask(Map<Integer, Task> taskList, int id)
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

        storage.save(JsonStorage.fromListOfTasksToJson(new ArrayList<>(taskList.values())));

        return task;
    }

    public Task updateTaskStatusById(int id, TaskStatus status)
    {
        Map<Integer, Task> taskList = getTaskList();

        Task task = findTask(taskList, id);

        task.setStatus(status);

        String now = LocalDateTime.now().format(Task.FORMATTER);
        task.setUpdatedAt(now);

        storage.save(JsonStorage.fromListOfTasksToJson(new ArrayList<>(taskList.values())));

        return task;
    }

    public Task deleteTaskById(int id)
    {
        Map<Integer, Task> taskList = getTaskList();

        Task task = findTask(taskList, id);

        taskList.remove(id);

        storage.save(JsonStorage.fromListOfTasksToJson(new ArrayList<>(taskList.values())));

        return task;
    }

    public List<Task> listAllTasks()
    {
        List<Task> tasks = new ArrayList<>(getTaskList().values());

        if (tasks.isEmpty()) {
            throw new NoTasksException("No tasks yet");
        } else {
            return tasks;
        }

    }

    public List<Task> listTasksByStatus(TaskStatus status)
    {
        List<Task> tasks = getTaskList().values().stream()
                .filter(task -> task.getStatus().equals(status)).toList();

        if (tasks.isEmpty()) {
            throw new NoTasksException("No tasks with status " + status.getTitle());
        } else {
            return tasks;
        }
    }
}