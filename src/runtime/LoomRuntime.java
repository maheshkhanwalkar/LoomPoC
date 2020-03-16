package runtime;

import java.util.ArrayList;
import java.util.List;

/**
 * Parallel runtime which takes advantage of the features provided by
 * Continuations implemented by Project Loom
 *
 * The runtime internally manages a task queue and OS worker threads, which will
 * periodically examine the task queue, pull a task off, and run it.
 *
 * However, often times, tasks may need to wait on other tasks to complete, which
 * generally implies blocking. This is problematic, since blocking a worker thread
 * reduces the overall level of parallelism available.
 *
 * This problem is solved by using continuations and some additional runtime machinery
 * to detect these dependencies on runtime and suspend the task -- resuming it when the
 * dependency has been resolved.
 */
public class LoomRuntime
{
    // Singleton reference
    private static LoomRuntime inst = new LoomRuntime();

    // Worker threads
    private List<Thread> workers = new ArrayList<>();

    // TODO add a ready task queue -- which are tasks that are ready to run
    // TODO add a waiting task queue -- tasks that are waiting on a condition

    private LoomRuntime()
    {
        int cores = Runtime.getRuntime().availableProcessors();

        // Create the worker threads
        for(int i = 0; i < cores; i++)
            workers.add(new Thread(this::workerMain));

        // Start the worker threads
        for (Thread worker : workers)
            worker.start();
    }

    /**
     * Get the singleton instance of the runtime
     * @return the runtime instance
     */
    public static LoomRuntime getInstance()
    {
        return inst;
    }

    public void submit(Task task)
    {
        // TODO submit this to the queue
    }

    /**
     * Main body for each worker thread within the runtime
     *
     * Each thread will attempt to pull a task off from the work
     * queue, if possible, and then begin to run it.
     *
     * On the condition that there are no available tasks to execute,
     * the worker thread will sleep until a task is added.
     */
    private void workerMain()
    {
        // TODO
    }
}
