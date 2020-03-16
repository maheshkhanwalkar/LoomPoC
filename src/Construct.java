public class Construct
{
    public static void main(String[] args)
    {
        ContinuationScope scope = new ContinuationScope("test");

        Continuation test = new Continuation(scope, () -> {
            System.out.println("Continuation here...");
            System.out.println("Yielding...");
            Continuation.yield(scope);
            System.out.println("Resumed...");
            System.out.println("Done...");
        });

        test.run();
        System.out.println("Main here");
        test.run();
        System.out.println("Back to main");
    }
}
