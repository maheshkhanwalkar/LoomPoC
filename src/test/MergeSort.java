package test;

import java.util.*;

import static runtime.Constructs.*;

public class MergeSort
{
    public static void main(String[] args) throws InterruptedException
    {
        int[] big = new int[1000000];
        int[] copy = new int[big.length];

        Random r = new Random();
        System.out.println("Starting sort...");

        // Setup the arrays with random data
        for(int j = 0; j < big.length; j++)
        {
            big[j] = r.nextInt();
            copy[j] = r.nextInt();
        }

        launchApp(() -> {
            long seqMin = Long.MAX_VALUE, parMin = Long.MAX_VALUE;
            int runs = 30;

            for(int i = 0; i < runs; i++)
            {
                long t1 = System.nanoTime();
                mergeSortSeq(copy, 0, copy.length);
                long t2 = System.nanoTime();

                ensureSorted(copy);

                if(t2 - t1 < seqMin)
                    seqMin = t2 - t1;

                t1 = System.nanoTime();
                mergeSort(big, 0, big.length);
                t2 = System.nanoTime();

                ensureSorted(big);

                if(t2 - t1 < parMin)
                    parMin = t2 - t1;

                shuffle(big);
                shuffle(copy);
            }

            System.out.println("[SEQ] " + seqMin / 1000.0);
            System.out.println("[PAR] " + parMin / 1000.0);
            System.out.println("Speedup: " + (double)seqMin / parMin);
        });
    }

    private static void shuffle(int[] data)
    {
        List<Integer> list = new ArrayList<>(data.length);

        for(int elem : data)
            list.add(elem);

        Collections.shuffle(list);

        for(int i = 0; i < list.size(); i++)
            data[i] = list.get(i);
    }

    public static void mergeSortSeq(int[] data, int start, int end)
    {
        // Limit recursive depth
        if(end - start <= 10) {
            Arrays.sort(data, start, end);
            return;
        }

        int mid = start + (end - start) / 2;

        mergeSort(data, start, mid);
        mergeSort(data, mid, end);

        merge(data, start, mid, end);
    }

    public static void mergeSort(int[] data, int start, int end)
    {
        // Limit recursive depth
        if(end - start <= 10) {
            Arrays.sort(data, start, end);
            return;
        }

        int mid = start + (end - start) / 2;

        finish(() -> {
           async(() -> mergeSort(data, start, mid));
           async(() -> mergeSort(data, mid, end));
        });

        merge(data, start, mid, end);
    }

    public static void ensureSorted(int[] data)
    {
        for(int i = 1; i < data.length; i++)
        {
            if(data[i] < data[i-1])
            {
                System.out.println("Not properly sorted");
                break;
            }
        }
    }

    private static void merge(int[] data, int start, int mid, int end)
    {
        int[] copy = new int[end - start];

        int p = 0;
        int i = start, j = mid;

        while(p < copy.length)
        {
            if(i < mid && j < end)
            {
                if(data[i] < data[j])
                {
                    copy[p] = data[i];
                    i++;
                }
                else
                {
                    copy[p] = data[j];
                    j++;
                }
            }
            else if(i < mid)
            {
                copy[p] = data[i];
                i++;
            }
            else
            {
                copy[p] = data[j];
                j++;
            }

            p++;
        }

        System.arraycopy(copy, 0, data, start, copy.length);
    }
}
