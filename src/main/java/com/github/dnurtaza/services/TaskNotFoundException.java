package com.github.dnurtaza.services;

public class TaskNotFoundException extends RuntimeException
{
    public TaskNotFoundException(int id)
    {
        super("Failed to find a task with ID: " + id);
    }
}