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
    private static final ContinuationScope SCOPE = new ContinuationScope("SCOPE");

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

        Task task = new Task(new Continuation(SCOPE, body));

        runtime.submitRoot(task);
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
     * One important point is that async {...} is always implicitly under
     * a finish scope. The following example illustrates this rule:
     *
     * Paradigm #1
     * async {
     *     async {
     *         [1]
     *     }
     *
     *     [2]
     * }
     *
     * Paradigm #2
     * async {
     *     finish {
     *         async {
     *            [1]
     *         }
     *
     *         [2]
     *     }
     * }
     *
     * The implementation here treats Paradigm #1 and #2 as the same. This
     * essentially simplifies task management, since parent tasks must always wait
     * on their children to complete.
     *
     * Therefore, to truly represent Paradigm #1 with the current implementation,
     * the innermost async task should be (manually) hoisted out:
     *
     * Paradigm #3
     *
     * async {
     *     [2]
     * }
     *
     * async {
     *     [1]
     * }
     *
     * This allows for the two async constructs to complete independently of each other,
     * without any waiting on each other.
     *
     * @param body - body of the async task
     */
    public static void async(Runnable body)
    {
        // TODO
    }
}
