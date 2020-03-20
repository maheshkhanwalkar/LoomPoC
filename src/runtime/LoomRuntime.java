package runtime;

import java.util.*;
import java.util.concurrent.*;
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
    private BlockingQueue<Task> tasks = new LinkedBlockingQueue<>();

    // Current task map
    private Map<Long, Task> current = new ConcurrentHashMap<>();

    // Shutdown flag
    private AtomicBoolean shutdown = new AtomicBoolean(false);

    // The initial task submitted
    private Task root;
    private ContinuationScope scope = new ContinuationScope("loom-runtime-scope");

    // Countdown latch -- used to wait until the root task is done
    private CountDownLatch latch = new CountDownLatch(1);

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
     * Submit the root task to the work queue
     * @param body - body of the root task to add
     */
    public void submitRoot(Runnable body)
    {
        Continuation cont = new Continuation(scope, () -> Constructs.finish(body));

        root = new Task(cont);
        tasks.offer(root);
    }

    /**
     * Wait until the root task completes
     * @throws InterruptedException - if the semaphore acquire is interrupted
     */
    public void waitOnRoot() throws InterruptedException
    {
        latch.await();
    }

    /**
     * Setup the provided task as an async construct
     *
     * The async subtask will be associated with the currently executing
     * parent task and will be added to the work queue.
     *
     * The parent task will then be returned to immediately, since the
     * parent does not directly wait on the spawned async until it reaches the
     * end of a finish scope (implicitly or explicitly)
     *
     * @param body - async subtask to setup
     */
    public void setupAsync(Runnable body)
    {
        Task child = new Task(new Continuation(scope, body));

        long tid = Thread.currentThread().getId();
        Task parent = current.get(tid);

        parent.addChild(child);
        tasks.offer(child);
    }

    /**
     * Setup the finish scope for this task
     *
     * The current task will enter a new finish scope, which is
     * done by pushing itself onto the finish stack.
     *
     * Any new async tasks that will be spawned will point to this task as
     * the finish scope that they reside in.
     *
     * @param body - body to encapsulate
     */
    public void setupFinish(Runnable body)
    {
        long tid = Thread.currentThread().getId();
        Task curr = current.get(tid);

        curr.finish.push(curr);
        body.run();
        curr.finish.pop();

        // Don't need to yield at all
        if(curr.count.get() == 0) {
            return;
        }

        // Yield until all the child async tasks have finished
        // Then, this continuation will be re-entered
        curr.status = TaskStatus.WAITING;
        Continuation.yield(scope);
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
        Task task;

        try
        {
            // Take a task from the queue
            // Since this is a blocking queue, the call will *block* until the queue
            // becomes non-empty
            task = tasks.take();
        }
        catch (InterruptedException e)
        {
            return false;
        }

        // Update the current map
        long tid = Thread.currentThread().getId();
        current.put(tid, task);

        // Run the task with this worker thread
        task.run();
        TaskStatus status = task.status;

        current.remove(tid);

        if(status == TaskStatus.COMPLETED)
        {
            Task finish;

            if(task == root || task == (finish = task.finish.peek()))
            {
                if(task.count.get() == 0) {
                    latch.countDown();
                    return true;
                }

                task.status = TaskStatus.WAITING;
                Continuation.yield(scope);
            }
            else
            {
                int count = finish.count.decrementAndGet();

                if(count == 0 && finish.status == TaskStatus.WAITING) {
                    finish.status = TaskStatus.READY;
                    tasks.offer(finish);
                }
            }

            return true;
        }

        return true;
    }
}
