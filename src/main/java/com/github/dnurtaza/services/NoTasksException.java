package com.github.dnurtaza.services;

public class NoTasksException extends RuntimeException
{
    public NoTasksException(String msg)
    {
        super(msg);
    }
}