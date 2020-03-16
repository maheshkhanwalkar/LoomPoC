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
    // TODO
}
