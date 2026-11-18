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
    @ParameterizedTest
    @MethodSource("escapeCases")
    void escapesSpecialCharacters(String input, String expected)
    {
        assertEquals(expected, JsonParser.adjust(input));
    }

    static Stream<Arguments> escapeCases()
    {
        return Stream.of(
                Arguments.of("Escapes \" quotes", "Escapes \\\" quotes"),
                Arguments.of("Escapes \\ back slashes", "Escapes \\\\ back slashes"),
                Arguments.of("Escapes \n new line", "Escapes \\n new line"),
                Arguments.of("Escapes \t horizontal tabs", "Escapes \\t horizontal tabs"),
                Arguments.of("Escapes \b backspaces", "Escapes \\b backspaces"),
                Arguments.of("Escapes \f new page", "Escapes \\f new page")
        );
    }

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
