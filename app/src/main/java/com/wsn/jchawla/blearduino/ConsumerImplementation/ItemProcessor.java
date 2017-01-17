package com.wsn.jchawla.blearduino.ConsumerImplementation;

/**
 * Created by jchawla on 24.10.2015.
 */
import android.util.Log;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class ItemProcessor implements Runnable
{
    private BlockingQueue< String> jobQueue;

    private volatile boolean keepProcessing;

    public ItemProcessor(BlockingQueue< String > queue)
    {
        jobQueue = queue;
        keepProcessing = true;
    }

    public void run()
    {
        while(keepProcessing || !jobQueue.isEmpty())
        {
            try
            {
                String j = jobQueue.poll(10, TimeUnit.SECONDS);

                if(j != null)
                {
                    Log.d("output",j);   //put J in a file here most probably
                }
            }
            catch(InterruptedException ie)
            {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    public void cancelExecution()
    {
        this.keepProcessing = false;
    }
}