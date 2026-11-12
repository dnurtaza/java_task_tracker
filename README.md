# java_task_tracker
Task tracker is a project used to track and manage your tasks. In this task, you will build a simple command line interface (CLI) to track what you need to do, what you have done, and what you are currently working on (https://roadmap.sh/projects/task-tracker).

### Usage
Setting up and list of commands with their usage
```bash
# Compilate files
# Make sure that you are in the project folder
javac *.java

# Adding a new task
java Main add "Buy groceries"
# Output: Task added successfully (ID: 1)

# Updating and deleting tasks
java Main task-cli update 1 "Buy groceries and cook dinner"
java Main delete 1

# Marking a task as in progress or done
java Main mark-in-progress 1
java Main mark-done 1

# Listing all tasks
java Main list

# Listing tasks by status
java Main list done
java Main list todo
java Main list in-progress
```

### Task Properties
Each task should have the following properties:

* ```id:``` A unique identifier for the task
* ```description:``` A short description of the task
* ```status:``` The status of the task (todo, in-progress, done)
* ```createdAt:``` The date and time when the task was created
* ```updatedAt:``` The date and time when the task was last updated