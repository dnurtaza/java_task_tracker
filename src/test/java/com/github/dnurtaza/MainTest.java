package com.github.dnurtaza;

import com.github.dnurtaza.models.Task;
import com.github.dnurtaza.models.TaskStatus;
import com.github.dnurtaza.services.NoTasksException;
import com.github.dnurtaza.services.TaskNotFoundException;
import com.github.dnurtaza.services.TaskService;
import com.github.dnurtaza.storage.JsonStorage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class MainTest
{
    private static final String THREE_TASKS = """
            [{"id": 1, "description": "Do homework", "status": "todo", "createdAt": "16-11-2026 10:37:00", "updatedAt": "16-11-2026 10:37:00"},
            {"id": 2, "description": "Buy food", "status": "in-progress", "createdAt": "16-11-2026 11:37:00", "updatedAt": "16-11-2026 16:37:00"},
            {"id": 3, "description": "Call mom", "status": "done", "createdAt": "16-11-2026 13:37:00", "updatedAt": "16-11-2026 15:37:00"}]""";

    @TempDir
    Path tempDir;

    private Path file;
    private TaskService service;

    private final PrintStream originalOut = System.out;
    private final ByteArrayOutputStream out = new ByteArrayOutputStream();

    @BeforeEach
    void setUp()
    {
        file = tempDir.resolve("tasks.json");
        service = new TaskService(new JsonStorage(file));
        System.setOut(new PrintStream(out, true, StandardCharsets.UTF_8));
    }

    @AfterEach
    void restoreOut()
    {
        System.setOut(originalOut);
    }

    private String output()
    {
        return out.toString(StandardCharsets.UTF_8);
    }

    private void withThreeTasks() throws IOException
    {
        Files.writeString(file, THREE_TASKS);
    }

    private void run(String... args)
    {
        Main.run(args, service);
    }

    @ParameterizedTest
    @CsvSource({"1, 1", "42, 42", "2147483647, 2147483647"})
    void parseIdAcceptsPositiveNumbers(String input, int expected)
    {
        assertEquals(expected, Main.parseId(input));
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc", "1.5", "", " ", "1a", "2147483648"})
    void parseIdRejectsNonNumbers(String input)
    {
        Main.UsageException e = assertThrows(Main.UsageException.class, () -> Main.parseId(input));

        assertTrue(e.getMessage().contains("must be a number"), e.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "-100"})
    void parseIdRejectsZeroAndNegative(String input)
    {
        Main.UsageException e = assertThrows(Main.UsageException.class, () -> Main.parseId(input));

        assertEquals("ID must be a positive number", e.getMessage());
    }

    @ParameterizedTest
    @CsvSource({
            "todo, TO_DO",
            "in-progress, IN_PROGRESS",
            "done, DONE",
            "DONE, DONE",
            "In-Progress, IN_PROGRESS"
    })
    void parseStatusAcceptsKnownTitlesIgnoringCase(String input, TaskStatus expected)
    {
        assertEquals(expected, Main.parseStatus(input));
    }

    @ParameterizedTest
    @ValueSource(strings = {"finished", "in_progress", "to-do", ""})
    void parseStatusRejectsUnknownTitles(String input)
    {
        Main.UsageException e = assertThrows(Main.UsageException.class, () -> Main.parseStatus(input));

        assertTrue(e.getMessage().contains("Unknown status"), e.getMessage());
    }


    @Test
    void requireArgsAcceptsExactCount()
    {
        assertDoesNotThrow(() -> Main.requireArgs(new String[]{"delete", "1"}, 2, "delete <id>"));
    }

    @Test
    void requireArgsRejectsTooFew()
    {
        Main.UsageException e = assertThrows(Main.UsageException.class,
                () -> Main.requireArgs(new String[]{"delete"}, 2, "delete <id>"));

        assertTrue(e.getMessage().startsWith("Not enough arguments"), e.getMessage());
        assertTrue(e.getMessage().contains("delete <id>"), e.getMessage());
    }

    @Test
    void requireArgsRejectsTooMany()
    {
        Main.UsageException e = assertThrows(Main.UsageException.class,
                () -> Main.requireArgs(new String[]{"delete", "1", "2"}, 2, "delete <id>"));

        assertTrue(e.getMessage().startsWith("Too many arguments"), e.getMessage());
    }


    @Test
    void requireNotBlankTrimsDescription()
    {
        assertEquals("Buy milk", Main.requireNotBlank("   Buy milk  "));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "   ", "\t", "\n"})
    void requireNotBlankRejectsBlank(String input)
    {
        assertThrows(Main.UsageException.class, () -> Main.requireNotBlank(input));
    }


    @Test
    void addCreatesTaskAndPrintsId()
    {
        run("add", "Buy groceries");

        assertEquals("Task \"Buy groceries\" was added successfully (ID: 1)", output().trim());
        assertEquals("Buy groceries", service.getTaskList().get(1).getDescription());
        assertEquals(TaskStatus.TO_DO, service.getTaskList().get(1).getStatus());
    }

    @Test
    void addTrimsDescription()
    {
        run("add", "   Buy groceries   ");

        assertEquals("Buy groceries", service.getTaskList().get(1).getDescription());
    }

    @Test
    void addKeepsQuotesAndUnicode()
    {
        run("add", "Купить \"молоко\" 🚀");

        assertEquals("Купить \"молоко\" 🚀", service.getTaskList().get(1).getDescription());
    }

    @Test
    void addContinuesAfterHighestId() throws IOException
    {
        withThreeTasks();

        run("add", "Go to gym");

        assertTrue(output().contains("(ID: 4)"), output());
    }

    @Test
    void addWithoutDescriptionThrowsAndCreatesNoFile()
    {
        assertThrows(Main.UsageException.class, () -> run("add"));
        assertFalse(Files.exists(file));
    }

    @Test
    void addWithBlankDescriptionThrowsAndCreatesNoFile()
    {
        assertThrows(Main.UsageException.class, () -> run("add", "   "));
        assertFalse(Files.exists(file));
    }

    @Test
    void addWithUnquotedWordsThrowsTooMany()
    {
        Main.UsageException e = assertThrows(Main.UsageException.class, () -> run("add", "Buy", "milk"));

        assertTrue(e.getMessage().startsWith("Too many arguments"), e.getMessage());
    }


    @Test
    void listPrintsEveryTaskOnItsOwnLine() throws IOException
    {
        withThreeTasks();

        run("list");

        String[] lines = output().trim().split("\\R");
        assertEquals(3, lines.length);
        assertTrue(lines[0].startsWith("ID: 1 | Description: Do homework"), lines[0]);
        assertTrue(lines[1].startsWith("ID: 2 | Description: Buy food"), lines[1]);
        assertTrue(lines[2].startsWith("ID: 3 | Description: Call mom"), lines[2]);
    }

    @Test
    void listOnEmptyStorageThrowsNoTasks()
    {
        assertThrows(NoTasksException.class, () -> run("list"));
    }

    @ParameterizedTest
    @CsvSource({"todo, 1", "in-progress, 2", "done, 3"})
    void listByStatusPrintsOnlyMatchingTasks(String status, int expectedId) throws IOException
    {
        withThreeTasks();

        run("list", status);

        String[] lines = output().trim().split("\\R");
        assertEquals(1, lines.length);
        assertTrue(lines[0].startsWith("ID: " + expectedId + " |"), lines[0]);
    }

    @Test
    void listWithUnknownStatusThrows() throws IOException
    {
        withThreeTasks();

        assertThrows(Main.UsageException.class, () -> run("list", "finished"));
        assertEquals("", output());
    }

    @Test
    void listWithTooManyArgumentsThrows() throws IOException
    {
        withThreeTasks();

        assertThrows(Main.UsageException.class, () -> run("list", "done", "todo"));
    }


    @Test
    void updateChangesDescriptionAndPrintsIt() throws IOException
    {
        withThreeTasks();

        run("update", "1", "Do math homework");

        assertEquals("Do math homework", service.getTaskList().get(1).getDescription());
        assertTrue(output().contains("ID: 1 | Description: \"Do math homework\""), output());
    }

    @Test
    void updateUnknownIdThrowsAndKeepsFile() throws IOException
    {
        withThreeTasks();

        assertThrows(TaskNotFoundException.class, () -> run("update", "99", "Nothing"));
        assertEquals(THREE_TASKS, Files.readString(file));
    }

    @Test
    void updateWithBadIdThrowsAndKeepsFile() throws IOException
    {
        withThreeTasks();

        assertThrows(Main.UsageException.class, () -> run("update", "abc", "Nothing"));
        assertEquals(THREE_TASKS, Files.readString(file));
    }

    @Test
    void updateWithoutDescriptionThrows() throws IOException
    {
        withThreeTasks();

        assertThrows(Main.UsageException.class, () -> run("update", "1"));
        assertEquals(THREE_TASKS, Files.readString(file));
    }

    @Test
    void updateWithBlankDescriptionThrows() throws IOException
    {
        withThreeTasks();

        assertThrows(Main.UsageException.class, () -> run("update", "1", "  "));
        assertEquals(THREE_TASKS, Files.readString(file));
    }


    @Test
    void deleteRemovesTaskAndPrintsId() throws IOException
    {
        withThreeTasks();

        run("delete", "2");

        assertFalse(service.getTaskList().containsKey(2));
        assertEquals(2, service.getTaskList().size());
        assertEquals("The task was deleted successfully (ID: 2)", output().trim());
    }

    @Test
    void deleteUnknownIdThrowsAndKeepsFile() throws IOException
    {
        withThreeTasks();

        assertThrows(TaskNotFoundException.class, () -> run("delete", "99"));
        assertEquals(THREE_TASKS, Files.readString(file));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "two"})
    void deleteWithInvalidIdThrows(String id) throws IOException
    {
        withThreeTasks();

        assertThrows(Main.UsageException.class, () -> run("delete", id));
        assertEquals(THREE_TASKS, Files.readString(file));
    }


    @Test
    void markInProgressChangesStatus() throws IOException
    {
        withThreeTasks();

        run("mark-in-progress", "1");

        assertEquals(TaskStatus.IN_PROGRESS, service.getTaskList().get(1).getStatus());
        assertTrue(output().contains("Status: \"in-progress\""), output());
    }

    @Test
    void markDoneChangesStatus() throws IOException
    {
        withThreeTasks();

        run("mark-done", "2");

        Task task = service.getTaskList().get(2);
        assertEquals(TaskStatus.DONE, task.getStatus());
        assertTrue(output().contains("Status: \"done\""), output());
    }

    @ParameterizedTest
    @ValueSource(strings = {"mark-in-progress", "mark-done"})
    void markUnknownIdThrows(String command) throws IOException
    {
        withThreeTasks();

        assertThrows(TaskNotFoundException.class, () -> run(command, "99"));
        assertEquals(THREE_TASKS, Files.readString(file));
    }

    @ParameterizedTest
    @ValueSource(strings = {"mark-in-progress", "mark-done"})
    void markWithoutIdThrows(String command)
    {
        assertThrows(Main.UsageException.class, () -> run(command));
    }


    @Test
    void unknownCommandPrintsErrorAndHelp()
    {
        Main.UsageException e = assertThrows(Main.UsageException.class, () -> run("foo"));
        assertEquals("Unknown command 'foo'", e.getMessage());

        assertThrows(Main.UsageException.class, () -> run("foo"));

        String help = output();
        assertTrue(help.startsWith("List of commands:"), help);
        for (Main.Command command : Main.commandList) {
            assertTrue(help.contains(command.syntax()), "Help is missing: " + command.caption());
        }
    }

    @Test
    void unknownCommandDoesNotTouchStorage()
    {
        assertThrows(Main.UsageException.class, () -> run("foo"));

        assertFalse(Files.exists(file));
    }

    @Test
    void commandsAreCaseSensitive()
    {
        assertThrows(Main.UsageException.class, () -> run("ADD", "Buy milk"));
        assertFalse(Files.exists(file));
    }
}