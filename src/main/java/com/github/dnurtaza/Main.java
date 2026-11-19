package com.github.dnurtaza;

import com.github.dnurtaza.models.Task;
import com.github.dnurtaza.models.TaskStatus;
import com.github.dnurtaza.services.NoTasksException;
import com.github.dnurtaza.services.TaskNotFoundException;
import com.github.dnurtaza.services.TaskService;
import com.github.dnurtaza.storage.CorruptedStorageException;
import com.github.dnurtaza.storage.JsonStorage;

import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.List;

public class Main
{
    record Command(String syntax, String caption) {}

    static final List<Command> commandList = List.of(
            new Command("add \"<description>\"", "Add a task with description \"<description>\""),
            new Command("list", "List all tasks"),
            new Command("list [todo|in-progress|done]", "List tasks with status [todo|in-progress|done]"),
            new Command("update <id> \"<description>\"", "Set description of a task with id <id> to \"<description>\""),
            new Command("delete <id>", "Delete a task with id <id>"),
            new Command("mark-in-progress <id>", "Set status of a task with id <id> to \"in-progress\""),
            new Command("mark-done <id>", "Set status of a task with id <id> to \"done\"")
    );

    public static void main(String[] args)
    {
        JsonStorage storage = new JsonStorage(Path.of("tasks.json"));
        TaskService service = new TaskService(storage);

        if (args.length > 0) {
            try {
                run(args, service);
            } catch (CorruptedStorageException e) {
                System.err.println("tasks.json is corrupted, nothing was changed: " + e.getMessage());
                System.exit(1);
            } catch (UsageException e) {
                System.err.println("Error: " + e.getMessage());
                System.exit(1);
            } catch (TaskNotFoundException | NoTasksException e) {
                System.err.println(e.getMessage());
                System.exit(1);
            } catch (UncheckedIOException e) {
                System.err.println("Could not access tasks.json: " + e.getCause().getMessage());
                System.exit(1);
            }
        } else {
            help();
        }
    }

    static void help()
    {
        System.out.println("List of commands:");
        commandList.forEach( command -> {
                System.out.println(command.syntax() + " | " + command.caption());
            }
        );
    }

    static void run(String[] args, TaskService service)
    {
        switch (args[0]) {
            case "add" -> {
                requireArgs(args, 2, "add \"<description>\"");
                Task task = service.addTask(requireNotBlank(args[1]));

                System.out.printf("Task \"%s\" was added successfully (ID: %d)%n", task.getDescription(), task.getId());
            }
            case "list" -> {
                if (args.length == 1) {
                    service.listAllTasks().forEach(task -> {
                        System.out.println(task.toString());
                    });
                    return;
                }

                requireArgs(args, 2, "list [todo|in-progress|done]");
                service.listTasksByStatus(parseStatus(args[1])).forEach(task -> {
                    System.out.println(task.toString());
                });
            }
            case "update" -> {
                requireArgs(args, 3, "update <id> \"<description>\"");
                Task task = service.updateTaskById(parseId(args[1]), requireNotBlank(args[2]));
                System.out.printf("Task description was updated successfully (ID: %d | Description: \"%s\")%n",
                        task.getId(), task.getDescription());
            }
            case "delete" -> {
                requireArgs(args, 2, "delete <id>");
                Task task = service.deleteTaskById(parseId(args[1]));
                System.out.printf("The task was deleted successfully (ID: %d)%n", task.getId());
            }
            case "mark-in-progress" -> {
                requireArgs(args, 2, "mark-in-progress <id>");
                Task task = service.updateTaskStatusById(parseId(args[1]), TaskStatus.IN_PROGRESS);
                System.out.printf("Status of the \"%s\" task was updated successfully (ID: %d | Status: \"%s\")%n",
                task.getDescription(), task.getId(), task.getStatus().getTitle());
            }
            case "mark-done" -> {
                requireArgs(args, 2, "mark-done <id>");
                Task task = service.updateTaskStatusById(parseId(args[1]), TaskStatus.DONE);
                System.out.printf("Status of the \"%s\" task was updated successfully (ID: %d | Status: \"%s\")%n",
                task.getDescription(), task.getId(), task.getStatus().getTitle());
            }
            default -> {
                help();
                throw new UsageException("Unknown command '" + args[0] + "'");
            }
        }
    }

    static int parseId(String i) {
        int id;

        try {
            id = Integer.parseInt(i);
        } catch (NumberFormatException e) {
            throw new UsageException("ID must be a number, but got '" + i + "'");
        }

        if (id <= 0) {
            throw new UsageException("ID must be a positive number");
        }

        return id;
    }

    static TaskStatus parseStatus(String s)
    {
        TaskStatus status = TaskStatus.fromTitle(s);

        if (status == null) {
            throw new UsageException("Unknown status '" + s + "'. Use one of: todo, in-progress, done");
        }

        return status;
    }

    static void requireArgs(String[] args, int expected, String syntax)
    {
        if (args.length < expected) {
            throw new UsageException("Not enough arguments. Usage: .\\task-cli " + syntax);
        } else if (args.length > expected) {
            throw new UsageException("Too many arguments. Usage: .\\task-cli " + syntax);
        }
    }

    static String requireNotBlank(String desc)
    {
        if (desc.isBlank()) {
            throw new UsageException("Description must not be empty");
        }

        return desc.trim();
    }

    static class UsageException extends RuntimeException
    {
        UsageException(String message)
        {
            super(message);
        }
    }
}