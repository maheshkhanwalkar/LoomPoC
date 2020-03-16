package runtime;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

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
    // Worker threads
    private List<Thread> workers = new ArrayList<>();

    // Task queue (ready-to-run)
    private Queue<Task> tasks = new ConcurrentLinkedQueue<>();

    // Waiting task set (self-map --> set)
    private Map<Task, Task> waiting = new ConcurrentHashMap<>();

    // Shutdown flag
    private AtomicBoolean shutdown = new AtomicBoolean(false);

    /**
     * Initialise the runtime, starting up all the initial worker
     * threads which will be ready to accept new tasks
     */
    public LoomRuntime()
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
     * Add a new task to the work queue
     * @param task - task to add
     */
    public void submit(Task task)
    {
        tasks.offer(task);
        tasks.notifyAll();
    }

    /**
     * Shutdown the runtime
     *
     * This method will cause the worker threads to be interrupted
     * and then joined, which will free the internal resources.
     *
     * However, any uncompleted tasks will be lost, so this method is
     * only truly safe when all tasks have completed and the runtime
     * is just sitting idle.
     *
     * @throws InterruptedException - the join on a worker thread was interrupted
     */
    public void shutdown() throws InterruptedException
    {
        // Interrupt the worker threads
        for(Thread worker : workers)
            worker.interrupt();

        shutdown.set(true);

        // Join on all the workers
        for(Thread worker : workers)
            worker.join();
    }

    /**
     * Dummy target for the worker thread
     */
    private void workerMain()
    {
        boolean loop = true;

        while(loop && !shutdown.get()) {
            loop = workerMainBody();
        }
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
    private boolean workerMainBody()
    {
        Task curr;

        try
        {
            // Wait until there is a task in the queue -- and remove it
            // from the queue
            while((curr = tasks.poll()) == null) {
                tasks.wait();
            }
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
            return false;
        }

        // Run the task with this worker thread
        curr.run();
        TaskStatus status = curr.getStatus();

        if(status == TaskStatus.COMPLETED)
        {
            Task parent = curr.getParent();

            // Nothing else needs to be done
            if(parent == null)
                return true;

            // TODO: is this even a valid state?
            //  This implies that the parent is not waiting on its child to complete
            //  which should only happen if an async contains another async, but does not
            //  wrap the inner async inside of a finish
            if(!waiting.containsKey(parent))
                return true;

            parent.updateStatus();
            TaskStatus pStatus = parent.getStatus();

            // Schedule the parent task
            if(pStatus == TaskStatus.READY) {
                waiting.remove(parent);
                tasks.offer(parent);
            }

            return true;
        }

        // Add the task to the waiting set
        if(status == TaskStatus.WAITING) {
            waiting.put(curr, curr);
            return true;
        }

        return true;
    }
}
