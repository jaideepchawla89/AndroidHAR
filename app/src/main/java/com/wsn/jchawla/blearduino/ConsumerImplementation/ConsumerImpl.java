package com.wsn.jchawla.blearduino.ConsumerImplementation;

/**
 * Created by jchawla on 24.10.2015.
 * This is the actually purported Implementation of a consumer
 * still need to fix the ItemProcessor to send data to a file or something
 */
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class ConsumerImpl implements Consumer
{
    private BlockingQueue< String > itemQueue =
            new LinkedBlockingQueue< String >();

    private ExecutorService executorService =
            Executors.newCachedThreadPool();

    private List< ItemProcessor > jobList =
            new LinkedList< ItemProcessor >();

    private volatile boolean shutdownCalled = false;

    public ConsumerImpl(int poolSize,LinkedBlockingQueue buf)
    {
        itemQueue =buf;

        for(int i = 0; i < poolSize; i++)
        {
            ItemProcessor jobThread =
                    new ItemProcessor(itemQueue);

            jobList.add(jobThread);
            executorService.submit(jobThread);
        }
    }

    public boolean consume(String j)
    {
        if(!shutdownCalled)
        {
            try
            {
                itemQueue.put(j);
            }
            catch(InterruptedException ie)
            {
                Thread.currentThread().interrupt();
                return false;
            }
            return true;
        }
        else
        {
            return false;
        }
    }

    public void finishConsumption()
    {
        shutdownCalled = true;

        for(ItemProcessor j : jobList)
        {
            j.cancelExecution();
        }

        executorService.shutdown();
    }
}