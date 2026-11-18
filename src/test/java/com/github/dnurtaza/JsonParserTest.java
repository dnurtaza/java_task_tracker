package com.github.dnurtaza;

import com.github.dnurtaza.storage.JsonParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JsonParserTest {
    @Test
    void parsesFourTasks()
    {
        String json = """
                [
                    {"id": 1, "description": "Do homework", "status": "todo", "createdAt": "16.11.2026 10:37", "updatedAt": "16.11.2026 10:37"},
                    {"id": 2, "description": "Buy food", "status": "in-progress", "createdAt": "16.11.2026 11:37", "updatedAt": "16.11.2026 16:37"},
                    {"id": 3, "description": "Go to gym food", "status": "todo", "createdAt": "16.11.2026 12:37", "updatedAt": "16.11.2026 12:37"},
                    {"id": 4, "description": "Call mom", "status": "done", "createdAt": "16.11.2026 13:37", "updatedAt": "16.11.2026 15:37"}
                ]
                """;

        List<Map<String, String>> expected = List.of(
                Map.of("id", "1",
                        "description", "Do homework",
                        "status", "todo",
                        "createdAt", "16.11.2026 10:37",
                        "updatedAt", "16.11.2026 10:37"),

                Map.of("id", "2",
                        "description", "Buy food",
                        "status", "in-progress",
                        "createdAt", "16.11.2026 11:37",
                        "updatedAt", "16.11.2026 16:37"),
                Map.of("id", "3",
                        "description", "Go to gym food",
                        "status", "todo",
                        "createdAt", "16.11.2026 12:37",
                        "updatedAt", "16.11.2026 12:37"),
                Map.of("id", "4",
                        "description", "Call mom",
                        "status", "done",
                        "createdAt", "16.11.2026 13:37",
                        "updatedAt", "16.11.2026 15:37")
        );

        assertEquals(expected, JsonParser.parseArray(json));
    }
}
