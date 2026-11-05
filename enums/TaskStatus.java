public enum TaskStatus
{
    TO_DO("todo"),
    IN_PROGRESS("in-progress"),
    DONE("done");

    private String title;

    TaskStatus(String title)
    {
        this.title = title;
    }

    public String getTitle()
    {
        return this.title;
    }
}