# Java Task Tracker
Task tracker is a project used to track and manage your tasks. In this task, you will build a simple command line interface (CLI) to track what you need to do, what you have done, and what you are currently working on (https://roadmap.sh/projects/task-tracker).

---

## Tech Stack
* **Java** (JDK 25)
* **Apache Maven** & **Maven Wrapper** (Build automation)
* **JUnit 6** (Unit testing)
* **Custom JSON Parser** (Zero-dependency local persistence via `tasks.json`)

---

## Task Properties
Each task should have the following properties:

* ```id:``` A unique identifier for the task
* ```description:``` A short description of the task
* ```status:``` The status of the task (todo, in-progress, done)
* ```createdAt:``` The date and time when the task was created
* ```updatedAt:``` The date and time when the task was last updated

---

## Prerequisites
To build and run this project, you only need:
* **Java Development Kit (JDK) 25** installed and configured in your system `PATH`.
* *Note: You do **not** need Apache Maven installed globally — the included Maven Wrapper (`mvnw`) handles the build automatically.*

---

## Installation & Build
1. **Clone the repository**
```bash
git clone https://github.com/dnurtaza/task_tracker.git
cd task_tracker
```
2. **Build the executable JAR using Maven Wrapper:**
* **Windows (PowerShell / CMD)**
    ```powershell 
    .\mvnw clean package
    ```
* **Linux / macOS:**
    ```bash
    ./mvnw clean package
    ```
    This will run the test suite and generate task_tracker-1.0-SNAPSHOT.jar inside the target/ directory.

---

## Usage
### Option 1: Using the Windows Wrapper Script (`task.bat`)
For convenience on Windows, use the provided `task.bat` script in the root directory:
```bash
# Adding a new task
.\task-cli add "Buy groceries"
# Output: Task added successfully (ID: 1)

# Updating and deleting tasks
.\task-cli update 1 "Buy groceries and cook dinner"
.\task-cli delete 1

# Marking a task as in progress or done
.\task-cli mark-in-progress 1
.\task-cli mark-done 1

# Listing all tasks
.\task-cli list

# Listing tasks by status
.\task-cli list done
.\task-cli list todo
.\task-cli list in-progress
```

### Option 2: Running the JAR directly
You can run the compiled JAR directly from any operating system using `java -jar`:
```bash
java -jar target/task_tracker-1.0-SNAPSHOT.jar <command> [arguments]
```

```bash
# Adding a new task
java -jar target/task_tracker-1.0-SNAPSHOT.jar add "Buy groceries"
# Output: Task added successfully (ID: 1)

# Updating and deleting tasks
java -jar target/task_tracker-1.0-SNAPSHOT.jar update 1 "Buy groceries and cook dinner"
java -jar target/task_tracker-1.0-SNAPSHOT.jar delete 1

# Marking a task as in progress or done
java -jar target/task_tracker-1.0-SNAPSHOT.jar mark-in-progress 1
java -jar target/task_tracker-1.0-SNAPSHOT.jar mark-done 1

# Listing all tasks
java -jar target/task_tracker-1.0-SNAPSHOT.jar list

# Listing tasks by status
java -jar target/task_tracker-1.0-SNAPSHOT.jar list done
java -jar target/task_tracker-1.0-SNAPSHOT.jar list todo
java -jar target/task_tracker-1.0-SNAPSHOT.jar list in-progress
```

---

## Data Storage
All tasks are automatically persisted in a local **`tasks.json`** file. You do not need to 
create this file manually — the application generates it automatically in your current working 
directory upon adding your first task.

---

## Author: `Daniyal Nurtaza` — [@dnurtaza](https://github.com/dnurtaza)