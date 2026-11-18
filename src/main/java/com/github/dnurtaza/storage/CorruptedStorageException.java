package com.github.dnurtaza.storage;

public class CorruptedStorageException extends RuntimeException
{
    public CorruptedStorageException(String message)
    {
        super(message);
    }

    public CorruptedStorageException(String message, Throwable cause)
    {
        super(message, cause);
    }
}