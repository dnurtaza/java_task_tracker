import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import com.github.dnurtaza.storage.JsonStorage;

public class JsonStorageTest
{
    @ParameterizedTest
    @MethodSource("escapeCases")
    void escapesSpecialCharacters(String input, String expected)
    {
        assertEquals(expected, JsonStorage.adjust(input));
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
}
