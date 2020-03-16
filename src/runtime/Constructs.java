package runtime;

/**
 * This class exposes various high-level parallelism constructs which is to be
 * used with the runtime.LoomRuntime for scheduling purposes.
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
    private static LoomRuntime runtime = LoomRuntime.getInstance();
}
