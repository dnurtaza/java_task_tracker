package com.github.dnurtaza.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Task
{
    private final int id;
    private String description;
    private TaskStatus status;
    private final String createdAt;
    private String updatedAt;

    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    public Task(int id, String description)
    {
        this(id, description, TaskStatus.TO_DO);
    }

    public Task(int id, String description, TaskStatus status)
    {
        this(id, description, status, LocalDateTime.now().format(FORMATTER));
    }

    private Task(int id, String description, TaskStatus status, String date)
    {
        this(id, description, status, date, date);
    }

    public Task(int id, String description, TaskStatus status, String createdAt, String updatedAt)
    {
        this.id = id;
        this.description = description;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public void touch()
    {
        this.updatedAt = LocalDateTime.now().format(FORMATTER);
    }

    public int getId() { return id; }
    public String getDescription() { return description; }
    public TaskStatus getStatus() { return status; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }

    public void setDescription(String description) { this.description = description; }
    public void setStatus(TaskStatus status) { this.status = status; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString()
    {
        return String.format("ID: %d | Description: %s | Status: %s | Created: %s | Updated: %s",
                getId(), getDescription(), getStatus().getTitle(), getCreatedAt(), getUpdatedAt()
        );
    }
}