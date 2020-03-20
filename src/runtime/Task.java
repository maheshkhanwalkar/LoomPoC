package runtime;

import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

public class Task
{
    // Underlying task representation
    public Continuation body;

    // The current status of the task
    public volatile TaskStatus status;

    // Any subtasks that this task depends on
    public AtomicInteger count = new AtomicInteger();

    // Finish scope
    public Stack<Task> finish = new Stack<>();

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
     * Add a child task and register it with the current finish scope
     * @param child - task to add
     */
    public void addChild(Task child)
    {
        child.finish.push(finish.peek());
        finish.peek().count.incrementAndGet();
    }
}
