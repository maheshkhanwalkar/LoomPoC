package test;

import static runtime.Constructs.*;

public class Simple
{
    public static void doWork(String before, String after, int wait)
    {
        try {
            System.out.println(before);
            Thread.sleep(wait);
            System.out.println(after);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws InterruptedException
    {
        launchApp(() -> {
            System.out.println("Entering finish #1");
            finish(() -> {
                async(() -> {
                    doWork("Async #1", "Async #1 Done", 1000);
                });

                async(() -> {
                    doWork("Async #2", "Async #2 Done", 800);
                });
            });

            System.out.println("Entering finish #2");
            finish(() -> {
                async(() -> {
                    doWork("Async #3", "Async #3 Done", 1000);
                });

                async(() -> {
                    doWork("Async #4", "Async #4 Done", 800);
                });
            });
        });
    }
}
