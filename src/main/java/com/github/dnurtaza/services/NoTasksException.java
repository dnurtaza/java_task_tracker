package com.github.dnurtaza.services;

public class NoTasksException extends RuntimeException
{
    public NoTasksException()
    {
        super("There are no tasks yet. ");
    }
}