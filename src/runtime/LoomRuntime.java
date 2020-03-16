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

    /**
     * TODO - actually do some initialisation here
     */
    private LoomRuntime()
    {
        int cores = Runtime.getRuntime().availableProcessors();

        // FIXME: there needs to be a supporting Runnable which
        //  handles pulling tasks off the shared queue and running them

        // Create the worker threads
        for(int i = 0; i < cores; i++)
            workers.add(new Thread());

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
}
