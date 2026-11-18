package com.github.dnurtaza;

import com.github.dnurtaza.storage.JsonParseException;
import com.github.dnurtaza.storage.JsonParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class JsonParserErrorTest
{
    @ParameterizedTest
    @ValueSource(strings = {
            "",                                   // empty input
            "   ",                                // only whitespace
            "{}",                                 // object instead of array
            "[",                                  // unterminated array
            "[{",                                 // unterminated object
            "[{\"a\": 1}",                        // missing ]
            "[{\"a\": 1},]",                      // trailing comma
            "[{\"a\": 1} {\"b\": 2}]",            // missing comma between objects
            "[{\"a\" 1}]",                        // missing colon
            "[{\"a\": }]",                        // missing value
            "[{a: 1}]",                           // key without quotes
            "[{\"a\": \"text}]",                  // unterminated string
            "[{\"a\": \"bad \\q escape\"}]",      // unknown escape \q
            "[{\"a\": \"\\u12\"}]",               // too short unicode escape
            "[{\"a\": \"\\uZZZZ\"}]",             // not hex digits in unicode escape
            "[] []",                              // data after the array
            "[{\"a\": 1}] x"                      // garbage after the array
    })
    void malformedJsonThrows(String json)
    {
        assertThrows(JsonParseException.class, () -> JsonParser.parseArray(json));
    }

    @Test
    void errorMessageContainsPosition()
    {
        JsonParseException e = assertThrows(JsonParseException.class,
                () -> JsonParser.parseArray("[{\"a\" 1}]"));

        assertTrue(e.getMessage().contains("position"), e.getMessage());
    }

    @Test
    void parsesEmptyArray()
    {
        assertEquals(List.of(), JsonParser.parseArray("[]"));
        assertEquals(List.of(), JsonParser.parseArray("  [ \n ]  "));
    }

    @Test
    void parsesEmptyObject()
    {
        assertEquals(List.of(Map.of()), JsonParser.parseArray("[{}]"));
    }

    @Test
    void parsesWithAnyWhitespace()
    {
        String json = "\n\t[ {\n \"id\" :\t1 ,\r\n \"status\":\"todo\" } ]\n";

        assertEquals(List.of(Map.of("id", "1", "status", "todo")), JsonParser.parseArray(json));
    }

    @Test
    void decodesEscapeSequences()
    {
        String json = """
                [{"d": "quote \\" backslash \\\\ slash \\/ newline \\n tab \\t unicode \\u0041\\u0436"}]""";

        String expected = "quote \" backslash \\ slash / newline \n tab \t unicode Aж";

        assertEquals(expected, JsonParser.parseArray(json).get(0).get("d"));
    }

    @Test
    void structuralCharactersInsideStringsAreNotSpecial()
    {
        String json = """
                [{"d": "a, b: {c} [d]"}]""";

        assertEquals("a, b: {c} [d]", JsonParser.parseArray(json).get(0).get("d"));
    }
}
