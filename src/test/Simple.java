package test;

import static runtime.Constructs.*;

public class Simple
{
    public static void main(String[] args) throws InterruptedException
    {
        launchApp(() -> {
            System.out.println("Hello world");
            System.out.println("This is a test of the launchApp()");
        });
    }
}
