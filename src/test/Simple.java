package test;

import static runtime.Constructs.*;

public class Simple
{
    public static void main(String[] args) throws InterruptedException
    {
        launchApp(() -> {
            async(() -> {
                try {
                    System.out.println("Async task #1 here");
                    Thread.sleep(1000);
                    System.out.println("Async #1 done");
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });

            async(() -> {
                try {
                    System.out.println("Async task #2 here");
                    Thread.sleep(800);
                    System.out.println("Async #2 done");
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });

            // FIXME: this sleep should not be needed, the current issue is
            //  that the runtime cannot handle implicit finish() scopes yet
            try {
                Thread.sleep(3000);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        });
    }
}
