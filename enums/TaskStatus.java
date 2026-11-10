package enums;

import java.util.Arrays;

public enum TaskStatus
{
    TO_DO("todo"),
    IN_PROGRESS("in-progress"),
    DONE("done");

    private final String title;

    TaskStatus(String title)
    {
        this.title = title;
    }

    public String getTitle()
    {
        return this.title;
    }

    public static TaskStatus fromTitle(String title)
    {
        if (title == null) {
            return null;
        }

        return Arrays.stream(TaskStatus.values())
                .filter(status -> status.title.equalsIgnoreCase(title.trim()))
                .findFirst()
                .orElse(null);
    }
}