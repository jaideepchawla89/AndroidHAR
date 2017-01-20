package com.wsn.jchawla.blearduino.ConsumerImplementation;

/**
 * Created by jchawla on 24.10.2015.
 */
import android.util.Log;

import com.wsn.jchawla.blearduino.Item.Item;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class ItemProcessor implements Runnable
{
    private BlockingQueue<Item> jobQueue;

    private volatile boolean keepProcessing;

    public ItemProcessor(BlockingQueue< Item > queue)
    {
        jobQueue = queue;
        keepProcessing = true;
        Log.d("Itemproce","gets created");
    }

    public void run()
    {
        while(keepProcessing || !jobQueue.isEmpty())
        {
            try
            {
                Item j = jobQueue.poll(10, TimeUnit.SECONDS);

                if(j != null)
                {
                    Log.d("output from",j.process());   //call j.process which is the process function implemented by the item class
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
        Log.d("Itemproc","Gets destroyed");
    }
}