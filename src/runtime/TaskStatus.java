package runtime;

public enum TaskStatus
{
    // The task is ready to execute
    READY,

    // The task is currently running
    RUNNING,

    // The task is currently waiting on its sub-tasks to finish
    WAITING,

    // The task has completed
    COMPLETED
}
