package com.github.dnurtaza.storage;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class JsonParser {
    private int pos = 0;
    private final String s;

    private JsonParser(String s)
    {
        this.s = s;
    }

    public static List<Map<String, String>> parseArray(String json)
    {
        JsonParser jp = new JsonParser(json);
        List<Map<String, String>> result = jp.readArray();

        jp.skipWhiteSpaces();

        if (jp.pos != jp.s.length()) {
            throw jp.error("Unexpected data after the end of the array");
        }

        return result;
    }

    private List<Map<String, String>> readArray()
    {
        List<Map<String, String>> list = new ArrayList<>();

        skipWhiteSpaces();
        expect('[');

        skipWhiteSpaces();

        if (peek() == ']') {
            pos++;
            return list;
        }

        while (true) {
            list.add(readObject());
            skipWhiteSpaces();

            char c = next();
            if (c == ']') break;
            if (c != ',') throw error("Expected ',' or ']' but found '" + c + "'");
        }

        return list;
    }

    private Map<String, String> readObject()
    {
        Map<String, String> object = new LinkedHashMap<>();

        skipWhiteSpaces();
        expect('{');
        skipWhiteSpaces();

        if (peek() == '}') {
            pos++;
            return object;
        }

        while (true) {
            skipWhiteSpaces();

            String key = readString();

            skipWhiteSpaces();
            expect(':');
            skipWhiteSpaces();

            object.put(key, readValue());
            skipWhiteSpaces();

            char c = next();
            if (c == '}') break;
            if (c != ',') throw error("Expected ',' or '}' but found '" + c + "'");
        }

        return object;
    }

    public static String adjust(String text)
    {
        StringBuilder sb = new StringBuilder();

        for (char c : text.toCharArray()) {
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                default -> {
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
                }
            }
        }

        return sb.toString();
    }

    private String readValue()
    {
        if (peek() == '"') return readString();

        int start = pos;
        while (pos < s.length() && ",}] \t\r\n".indexOf(s.charAt(pos)) == -1) {
            pos++;
        }

        if (start == pos) throw error("Expected a value");

        return s.substring(start, pos);
    }

    private String readString()
    {
        expect('"');

        StringBuilder sb = new StringBuilder();

        while (true) {
            if (pos >= s.length()) throw error("Unterminated string");

            char c = s.charAt(pos++);

            if (c == '"') return sb.toString();

            if (c != '\\') {
                sb.append(c);
                continue;
            }

            if (pos >= s.length()) throw error("Unterminated escape sequence");
            char e = s.charAt(pos++);

            switch (e) {
                case '"' -> sb.append('"');
                case '\\' -> sb.append('\\');
                case '/' -> sb.append('/');
                case 'n' -> sb.append('\n');
                case 'r' -> sb.append('\r');
                case 't' -> sb.append('\t');
                case 'b' -> sb.append('\b');
                case 'f' -> sb.append('\f');
                case 'u' -> {
                    if (pos + 4 > s.length()) throw error("Invalid \\u escape");
                    try {
                        sb.append((char) Integer.parseInt(s.substring(pos, pos + 4), 16));
                    } catch (NumberFormatException exc) {
                        throw error("Invalid \\u escape");
                    }

                    pos += 4;
                }
                default -> throw error("Unknown escape sequence \\" + e);
            }
        }
    }

    private void skipWhiteSpaces()
    {
        while (pos < s.length() && Character.isWhitespace(s.charAt(pos))) pos++;
    }

    private char next()
    {
        if (pos >= s.length()) throw error("Unexpected end of JSON");
        else return s.charAt(pos++);
    }

    private void expect(char expected)
    {
        char c = next();

        if (c != expected) {
            pos--;
            throw error("Expected '" + expected + "' but found '" + c + "'");
        }
    }

    private char peek()
    {
        return pos < s.length() ? s.charAt(pos) : '\0';
    }

    private IllegalArgumentException error(String msg)
    {
        return new IllegalArgumentException(msg + " (position " + pos + ")");
    }
}
