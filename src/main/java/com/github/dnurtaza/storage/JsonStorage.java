package com.github.dnurtaza.storage;

import com.github.dnurtaza.models.TaskStatus;

import java.io.UncheckedIOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.nio.file.Files;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
            throw new UncheckedIOException("Failed to write " + path, e);
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
            throw new UncheckedIOException("Failed to read " + path, e);
        }
    }

    public static Object getJsonFieldValue(Object val)
    {
        if (val == null) {
            return "null";
        } else if (val instanceof Number) {
            return val.toString();
        } else if (val instanceof TaskStatus status) {
            return "\"" + status.getTitle() + "\"";
        } else {
            return "\"" + JsonParser.adjust(val.toString()) + "\"";
        }
    }

    public static String fromListOfObjectsToJson(List<?> objectList)
    {
        if (objectList.isEmpty()) return "[]";

        List<String> json = new ArrayList<>();

        objectList.forEach(object -> {
            json.add(fromObjectToJson(object));
        });

        return "[" + String.join(", ", json) + "]";
    }

    public static String fromObjectToJson(Object object)
    {
        List<String> json = new ArrayList<>();
        Field[] fields = object.getClass().getDeclaredFields();

        for (Field field : fields) {
            int modifiers = field.getModifiers();
            if (Modifier.isStatic(modifiers) || Modifier.isTransient(modifiers)) {
                continue;
            }

            try {
                field.setAccessible(true);
                Object value = field.get(object);

                json.add("\"" + field.getName() + "\": " + getJsonFieldValue(value));
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Failed while accessing the field: " + field);
            }
        }

        return "{" + String.join(", ", json) + "}";
    }
}