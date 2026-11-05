import enums.TaskStatus;

class TaskTracker
{
    private int id;
    private String description;
    private TaskStatus status;
    private String createdAt;
    private String updatedAt;

    TaskTracker(int id, String description, TaskStatus status, String createdAt, String updatedAt)
    {
        this.id = id;
        this.description = description;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public int getId() { return id; }
    public String getDescription() { return description; }
    public TaskStatus getStatus() { return status; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }

    public void setId(int id) { this.id = id; }
    public void setDescription(String description) { this.description = description; }
    public void setStatus(TaskStatus status) { this.status = status; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}