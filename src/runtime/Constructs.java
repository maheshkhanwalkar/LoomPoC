package runtime;

/**
 * This class exposes various high-level parallelism constructs which is to be
 * used with the LoomRuntime for scheduling purposes.
 *
 * The two main constructs that will be implemented are: finish and async, which
 * are described in detail, below.
 *
 * The following pseudo-code snippet details the main use-case of async-finish:
 *
 * finish {
 *    async {
 *        [...]
 *    }
 *    ...
 *    async {
 *        [...]
 *    }
 *    ...
 * }
 *
 * Each 'async' is encapsulated as a task, which can be executed by one of the worker
 * threads within the runtime. The 'finish' will wait for each of its child 'async's
 * to complete before returning.
 *
 * An important feature of this constructs is the fact that they can be nested in nature
 * to support complex structures of parallelism.
 *
 * For simplicity and ease-of-use, it makes sense to import static this class, so that its
 * static methods can be referenced without the class identifier. This allows for the actual
 * code to visually match the pseudo-code (when using lambdas):
 *
 * finish(() -> {
 *    async(() -> {
 *        [...]
 *    });
 *    ...
 *    async(() -> {
 *        [...]
 *    });
 *    ...
 * });
 *
 */
public class Constructs
{
    private static LoomRuntime runtime;

    /**
     * Launch a new "application" to run on the runtime framework
     * @param body - root body of the task
     * @throws InterruptedException - if the wait-to-finish is interrupted
     */
    public static void launchApp(Runnable body) throws InterruptedException
    {
        // Only one runtime can be launched at atime
        if(runtime != null)
            return;

        runtime = new LoomRuntime();
        runtime.submitRoot(body);
        runtime.waitOnRoot();
        runtime.shutdown();
    }

    /**
     * Spawn an async task to execute the body
     *
     * This call should only be executed within the launchApp { ... }
     * scope, since the Loom Runtime needs to be initialised.
     *
     * The 'body' specified will be wrapped within a new task and will
     * be scheduled to execute independently.
     *
     * @param body - body of the async task
     */
    public static void async(Runnable body)
    {
        runtime.setupAsync(body);
    }

    /**
     * Define a finish scope encapsulating the body
     *
     * This call should only be called within the launchApp {...} scope, since it
     * requires the Loom Runtime to be initialised.
     *
     * All instructions execute sequentially within the body, except for async { ... }
     * calls, which will spawn independent tasks to execute alongside the original thread.
     *
     * However, once the finish { ... } body is done, it will wait on any outstanding async
     * calls which still need to complete.
     *
     * @param body - encapsulated body within the finish
     */
    public static void finish(Runnable body)
    {
       runtime.setupFinish(body);
    }
}
