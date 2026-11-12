import enums.TaskStatus;

class Main
{
    public static void main(String[] args)
    {
        if (args.length > 0) {
            TaskService.initializeIdCounter();

            switch (args[0]) {
                case "add" -> {
                    if (args.length <= 1) return;

                    String desc = args[1].replace("\"", "");
                    TaskService.addTask(new Task(TaskService.getIdCounter() + 1, desc));
                }
                case "list" -> {
                    if (args.length == 1) {
                        TaskService.listAllTasks();
                        return;
                    }

                    TaskService.listTasksByStatus(TaskStatus.fromTitle(args[1]));
                }
                case "update" -> {
                    if (args.length <= 2) return;

                    TaskService.updateTaskById(Integer.parseInt(args[1]), args[2].replace("\"", ""));
                }
                case "delete" -> {
                    if (args.length <= 1) return;

                    TaskService.deleteTaskById(Integer.parseInt(args[1]));
                }
                case "mark-in-progress" -> {
                    if (args.length <= 1) return;

                    TaskService.updateTaskStatusById(Integer.parseInt(args[1]), TaskStatus.IN_PROGRESS);
                }
                case "mark-done" -> {
                    if (args.length <= 1) return;

                    TaskService.updateTaskStatusById(Integer.parseInt(args[1]), TaskStatus.DONE);
                }
            }
        }
    }
}