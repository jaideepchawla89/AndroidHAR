package com.wsn.jchawla.blearduino.Main;

import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

/**
 * Created by jchawla on 16.01.2017.
 * possibly redundant class
 */

public class TestReceiver extends WakefulBroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent service = new Intent(context, SensorIntentService.class);
        Log.i("SimpleWakefulReceiver", "Starting service @ " + SystemClock.elapsedRealtime());

        startWakefulService(context, service);

    }
}
