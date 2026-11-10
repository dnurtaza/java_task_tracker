import java.util.Map;
import java.util.HashMap;

import enums.TaskStatus;

class TaskService
{
    public static String fromTaskListToJson(Map<Integer, Task> taskList)
    {
        if (taskList.isEmpty()) return "[]";

        String json = "[";

        for (Task task : taskList.values()) {
            json += task.toJson() + ",";
        }

        json += "]";

        return json;
    }

    public static Map<Integer, Task> getTaskList()
    {
        Map<Integer, Task> taskList = new HashMap<>();

        String content = JsonStorage.readFromJson();

        if (content.equals("[]")) return taskList;

        String[] objects = content.substring(1, content.length() - 1).trim().split("\\},\\s*\\{");

        for (String obj : objects) {
            obj = obj.replace("{", "").replace("}", "");
            String[] fields = obj.split(",\\s*\"");

            int id = 0;
            String desc = "";
            TaskStatus status = null;
            String createdAt = "";
            String updatedAt = "";

            String[] kv = new String[2];
            for (String field : fields) {
                kv = field.replace("\"", "").trim().split("\\s*:\\s*", 2);

                if (kv.length < 2) { break; }

                String key = kv[0];
                String val = kv[1];

                switch (key) {
                    case "id" -> id = Integer.parseInt(val);
                    case "description" -> desc = val;
                    case "status" -> status = TaskStatus.fromTitle(val);
                    case "createdAt" -> createdAt = val;
                    case "updatedAt" -> updatedAt = val;
                }
            }

            if (kv.length < 2) { continue; }

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
    }

    public static void updateTaskById(int id, String desc)
    {
        Map<Integer, Task> taskList = getTaskList();

        if (taskList.isEmpty() || !taskList.containsKey(id)) return;

        taskList.get(id).setDescription(desc);

        JsonStorage.saveToJson(fromTaskListToJson(taskList));
    }

    public static void updateTaskStatusById(int id, TaskStatus status)
    {
        Map<Integer, Task> taskList = getTaskList();

        if (taskList.isEmpty() || !taskList.containsKey(id)) return;

        taskList.get(id).setStatus(status);

        JsonStorage.saveToJson(fromTaskListToJson(taskList));
    }

    public static void deleteTaskById(int id)
    {
        Map<Integer, Task> taskList = getTaskList();

        if (taskList.isEmpty() || !taskList.containsKey(id)) return;

        taskList.remove(id);

        JsonStorage.saveToJson(fromTaskListToJson(taskList));
    }

    public static void listTasksByStatus(TaskStatus status)
    {
        Map<Integer, Task> taskList = getTaskList();

        if (taskList.isEmpty()) return;

        taskList.values()
                .stream()
                .filter(task -> task.getStatus().equals(status))
                .forEach(task -> System.out.println(
                        String.format("ID: %d | Description: %s", task.getId(), task.getDescription())
                ));
    }
}