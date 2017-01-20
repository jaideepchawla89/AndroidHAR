package com.wsn.jchawla.blearduino.Main;

import android.app.Notification;
import android.app.Service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.SensorEvent;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.SyncStateContract;
import android.util.Log;
import android.widget.Toast;

import com.wsn.jchawla.blearduino.ConsumerImplementation.ItemProcessor;
import com.wsn.jchawla.blearduino.Producer.PhoneProducer;
import com.wsn.jchawla.blearduino.R;

import java.util.concurrent.LinkedBlockingQueue;


/**
 * Created by jchawla on 22.09.2015.
 */
public class SensorService extends Service {



    public static final String TAG = SensorService.class.getName();
    public static final int SCREEN_OFF_RECEIVER_DELAY = 500;
    private PowerManager.WakeLock mWakeLock= null;


    LinkedBlockingQueue<String> sensorData=new LinkedBlockingQueue<>();


    PhoneProducer phoneProducer  ;
    ItemProcessor itemProcessor;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     *  Used to name the worker thread, important only for debugging.
     */

    //  BluetoothProducer  b ;
    // BluetoothProducer  b = new BluetoothProducer(this,sensorData);
    //  Consumer c = new ConsumerImpl(10,sensorData);





    @Override
    public void onCreate()
    {
        super.onCreate();
        Toast.makeText(this, "Service created", Toast.LENGTH_LONG).show();
        PowerManager manager =
                (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = manager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);

        registerReceiver(mReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));

        Log.d("service", "service oncreate");

    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId){


       // String pName = intent.getStringExtra("personName");
        //String activityName = intent.getStringExtra("activityName");
        Notification notification = new Notification.Builder(this)
                .setContentTitle(AppConstants.ACTIVITY.NOTIFICATION_TITLE)
                .setContentText(AppConstants.ACTIVITY.NOTIFICATION_MESSAGE)
                .build();


        startForeground(AppConstants.NOTIFICATION_ID.FOREGROUND_SERVICE, notification); //possibly need to fix this

       // p= new PhoneProducer(this,sensorData,pName,activityName);
        // b= new BluetoothProducer(this,sensorData,pName,activityName);
        phoneProducer= new PhoneProducer(this,sensorData,"hard","code");
        itemProcessor = new ItemProcessor(sensorData);

        new Thread(phoneProducer).start();
        new Thread (itemProcessor).start();
        Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();
        Log.d("service", "service onstartcommand");

        mWakeLock.acquire();



        return START_STICKY;
    }

    public BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "onReceive("+intent+")");

            if (!intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                return;
            }

            Runnable runnable = new Runnable() {
                public void run() {
                    Log.i(TAG, "Runnable executing.");
                    //unregisterListener(); possibly reregister all threads
                    //registerListener();
                    phoneProducer.cleanThread();
                    //new Thread(p).start();
                    // This might lead to creation of many threads and needs to be checked
                    new Thread(phoneProducer).start();

                }
            };

            new Handler().postDelayed(runnable, SCREEN_OFF_RECEIVER_DELAY);
        }
    };



    public void onStop(){
        //  b.stopBluetooth();

       // p.cleanThread();


        //   c.finishConsumption();


    }



    public void onPause(){

        // b.stopBluetooth();

      //  p.cleanThread();

        //   c.finishConsumption();
    }



    @Override
    public void onDestroy(){
        super.onDestroy();

        // b.stopBluetooth();
        unregisterReceiver(mReceiver);
        phoneProducer.cleanThread();
        itemProcessor.cancelExecution();

        mWakeLock.release();
        stopForeground(true);

        //   c.finishConsumption();
    }




    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.

        //throw new UnsupportedOperationException("Not yet implemented");
        return null;
    }




}
