package runtime;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Task
{
    // Underlying task representation
    private Continuation body;

    // The current status of the task
    private TaskStatus status;

    // Any subtasks that this task depends on
    private Queue<Task> children = new ConcurrentLinkedQueue<>();

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

        // The underlying continuation has yielded
        status = TaskStatus.WAITING;
    }

    /**
     * Update the task's status
     *
     * The task only becomes ready again once all of its children have finished.
     * Therefore, this method should be called whenever a child task has a status update.
     */
    public void updateStatus()
    {
        // Ensure all the subtasks have completed
        for(Task child : children)
        {
            if(child.status != TaskStatus.COMPLETED)
                return;
        }

        // Every subtask has completed, so reschedule the parent task
        status = TaskStatus.READY;
    }

    /**
     * Add a child task to this task's internal subtask list
     * @param child - task to add
     */
    public void addChild(Task child)
    {
        child.parent = this;
        this.children.add(child);
    }

    /**
     * Set the task's status
     * @param status - new status
     */
    public void setStatus(TaskStatus status)
    {
        this.status = status;
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
