package com.github.dnurtaza;

import com.github.dnurtaza.models.Task;
import com.github.dnurtaza.models.TaskStatus;
import com.github.dnurtaza.services.TaskService;
import com.github.dnurtaza.storage.JsonStorage;

import java.nio.file.Path;

public class Main
{
    public static void main(String[] args)
    {
        JsonStorage storage = new JsonStorage(Path.of("tasks.json"));
        TaskService taskService = new TaskService(storage);

        if (args.length > 0) {
            switch (args[0]) {
                case "add" -> {
                    if (args.length == 1) return;

                    String desc = args[1].replace("\"", "");
                    taskService.addTask(desc);
                }
                case "list" -> {
                    if (args.length == 1) {
                        taskService.listAllTasks();
                        return;
                    }

                    taskService.listTasksByStatus(TaskStatus.fromTitle(args[1]));
                }
                case "update" -> {
                    if (args.length <= 2) return;

                    taskService.updateTaskById(Integer.parseInt(args[1]), args[2].replace("\"", ""));
                }
                case "delete" -> {
                    if (args.length == 1) return;

                    taskService.deleteTaskById(Integer.parseInt(args[1]));
                }
                case "mark-in-progress" -> {
                    if (args.length == 1) return;

                    taskService.updateTaskStatusById(Integer.parseInt(args[1]), TaskStatus.IN_PROGRESS);
                }
                case "mark-done" -> {
                    if (args.length == 1) return;

                    taskService.updateTaskStatusById(Integer.parseInt(args[1]), TaskStatus.DONE);
                }
            }
        }
    }
}