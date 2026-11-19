package com.github.dnurtaza.services;

import java.util.*;

import com.github.dnurtaza.models.Task;
import com.github.dnurtaza.models.TaskStatus;
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
        return storage.load();
    }

    public Task addTask(String description)
    {
        Map<Integer, Task> taskList = getTaskList();
        int id = nextId(taskList);

        Task task = new Task(id, description);
        taskList.put(id, task);

        storage.save(taskList);

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

        task.touch();

        storage.save(taskList);

        return task;
    }

    public Task updateTaskStatusById(int id, TaskStatus status)
    {
        Map<Integer, Task> taskList = getTaskList();

        Task task = findTask(taskList, id);

        task.setStatus(status);

        task.touch();

        storage.save(taskList);

        return task;
    }

    public Task deleteTaskById(int id)
    {
        Map<Integer, Task> taskList = getTaskList();

        Task task = findTask(taskList, id);

        taskList.remove(id);

        storage.save(taskList);

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