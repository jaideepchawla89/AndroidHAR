package com.wsn.jchawla.blearduino.Main;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.wsn.jchawla.blearduino.R;

public class MainActivity extends AppCompatActivity {

    EditText pName;
    EditText activityName;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void startMonitor(View view) {

        pName = (EditText) findViewById(R.id.personName);
        activityName = (EditText) findViewById(R.id.activityName);


        Intent serviceIntent = new Intent(this, TestReceiver.class);
        serviceIntent.putExtra("personName",pName.getText().toString());
        serviceIntent.putExtra("activityName",activityName.getText().toString());

        startService(serviceIntent);

        //sendBroadcast(serviceIntent);
    }


    public void stopMonitor(View view) {
        Intent serviceIntent = new Intent(this, SensorService.class);
        stopService(serviceIntent);
    }
}