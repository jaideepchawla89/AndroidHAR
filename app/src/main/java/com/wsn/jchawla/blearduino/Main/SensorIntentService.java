package com.wsn.jchawla.blearduino.Main;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.wsn.jchawla.blearduino.Producer.PhoneProducer;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by jchawla on 16.01.2017.
 */

public class SensorIntentService extends IntentService {


    LinkedBlockingQueue<String> sensorData=new LinkedBlockingQueue<>();
    PhoneProducer p;


    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public SensorIntentService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String pName = intent.getStringExtra("personName");
        String activityName = intent.getStringExtra("activityName");

       // p= new PhoneProducer(this,sensorData,pName,activityName);
        // b= new BluetoothProducer(this,sensorData,pName,activityName);



        Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();
        Log.d("service", "service onstartcommand");

        //setupFolderAndFile();
        //   b.startBluetooth();

        new Thread(p).start();

    }
}
