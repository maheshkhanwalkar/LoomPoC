package runtime;

import java.util.LinkedList;
import java.util.List;

public class Task
{
    // TODO implement the creation of subtasks
    // TODO support status updating from WAITING to READY

    // Underlying task representation
    private Continuation body;

    // The current status of the task
    private TaskStatus status;

    // Any subtasks that this task depends on
    private List<Task> subTasks = new LinkedList<>();

    // The parent task (if it exists)
    private Task parent;

    /**
     * Initialise a task
     * @param body - underlying representation
     */
    public Task(Continuation body)
    {
        this.body = body;
        this.status = TaskStatus.READY;
    }

    /**
     * Execute the task
     *
     */
    public void run()
    {
        // The task is not actually ready, so don't try to run it
        // This is more a safety mechanism -- ideally this condition should never be true
        if(status != TaskStatus.READY) {
            return;
        }

        status = TaskStatus.RUNNING;
        body.run();

        // Finished executing
        if(body.isDone()) {
            status = TaskStatus.COMPLETED;
            return;
        }

        // The underlying continuation yielded
        status = TaskStatus.WAITING;
    }

    public void updateStatus()
    {
        // TODO
    }

    /**
     * Get the current status of the task
     * @return the status
     */
    public TaskStatus getStatus()
    {
        return status;
    }

    /**
     * Get the task's parent (if it exists)
     * @return the parent, or null if it doesn't exist
     */
    public Task getParent()
    {
        return parent;
    }
}
