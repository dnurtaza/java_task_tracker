package com.github.dnurtaza.storage;

import java.nio.file.Path;
import java.nio.file.Files;
import java.io.IOException;

public class JsonStorage {
    private final Path path;

    public JsonStorage(Path path) {
        this.path = path;
    }

    public void save(String json)
    {
        try {
            Files.writeString(this.path, json);
        } catch (IOException e) {
            System.err.println("Error, while trying to write: " + e.getMessage());
        }
    }

    public String read()
    {
        if (!Files.exists(path)) {
            return "[]";
        }

        try {
            return Files.readString(path).trim();
        } catch (IOException e) {
            System.err.println("Error, while trying to read: " + e.getMessage());
            return "[]";
        }
    }
}