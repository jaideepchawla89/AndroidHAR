package com.wsn.jchawla.blearduino.Producer;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.wsn.jchawla.blearduino.Main.AppConstants;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by jchawla on 26.09.2015.
 */
public class BluetoothProducer {

    // UUIDs for UAT service and associated characteristics.
    public static UUID UART_UUID = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E");
    public static UUID TX_UUID = UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E");
    public static UUID RX_UUID = UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E");
    // UUID for the BTLE client characteristic which is necessary for notifications.
    public static UUID CLIENT_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    // BTLE state
    private BluetoothAdapter adapter;
    private BluetoothGatt gatt;
    private BluetoothGattCharacteristic tx;
    private BluetoothGattCharacteristic rx;
    private Context mContext;
    private LinkedBlockingQueue<String> buffer=new LinkedBlockingQueue<>();

    public static File mLogFile = null;
    public static FileOutputStream mFileStream = null;

    private long timestamp ;
    private String pName;
    private String activityname;


    public BluetoothProducer(Context context, LinkedBlockingQueue buf, String name, String aName){
        adapter = BluetoothAdapter.getDefaultAdapter();

        if(adapter.isEnabled()) {

            Log.d("message", "bluetooth gets instantiated");
        }
        else
        {
            adapter.enable();

            Log.d("message", "bluetooth gets instantiated,was off");
        }

        mContext = context;
        buffer = buf;
        pName=name;
        activityname=aName;

     setupFolderAndFile();

    }


    public void stopBluetooth(){

        if (gatt != null) {
            // For better reliability be careful to disconnect and close the connection.
            gatt.disconnect();
            gatt.close();
            gatt = null;
            tx = null;
            rx = null;
        }
          shutdownLogger();
        Log.d("BLEmessage","destroying BLE");
    }



    private BluetoothGattCallback callback = new BluetoothGattCallback() {
        // Called whenever the device connection state changes, i.e. from disconnected to connected.
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (newState == BluetoothGatt.STATE_CONNECTED) {
                writeLine("Connected!");
                // Discover services.
                if (!gatt.discoverServices()) {
                    writeLine("Failed to start discovering services!");
                }
            }
            else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                writeLine("Disconnected!");
            }
            else {
                writeLine("Connection state changed.  New state: " + newState);
            }
        }

        // Called when services have been discovered on the remote device.
        // It seems to be necessary to wait for this discovery to occur before
        // manipulating any services or characteristics.
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                writeLine("Service discovery completed!");
            }
            else {
                writeLine("Service discovery failed with status: " + status);
            }
            // Save reference to each characteristic.
            tx = gatt.getService(UART_UUID).getCharacteristic(TX_UUID);
            rx = gatt.getService(UART_UUID).getCharacteristic(RX_UUID);
            // Setup notifications on RX characteristic changes (i.e. data received).
            // First call setCharacteristicNotification to enable notification.
            if (!gatt.setCharacteristicNotification(rx, true)) {
                writeLine("Couldn't set notifications for RX characteristic!");
            }
            // Next update the RX characteristic's client descriptor to enable notifications.
            if (rx.getDescriptor(CLIENT_UUID) != null) {
                BluetoothGattDescriptor desc = rx.getDescriptor(CLIENT_UUID);
                desc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                if (!gatt.writeDescriptor(desc)) {
                    writeLine("Couldn't write RX client descriptor value!");
                }
            }
            else {
                writeLine("Couldn't get RX client descriptor!");
            }
        }

        // Called when a remote characteristic changes (like the RX characteristic).
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            //writeLine("Received: " + characteristic.getStringValue(0));
            timestamp = System.currentTimeMillis()/1000;

           String writeToFile = characteristic.getStringValue(0);

            if(writeToFile.contains("a"))
            {

              writeToFile= writeToFile.replaceAll("a",String.valueOf(timestamp)+","+pName+","+activityname);


//Possibly use string tokenizer here
            }
             writeToFile(writeToFile);
            Log.d("received",writeToFile);
        }
    };

    private BluetoothAdapter.LeScanCallback scanCallback = new BluetoothAdapter.LeScanCallback() {
            // Called when a device is found.
            @Override
            public void onLeScan(BluetoothDevice bluetoothDevice, int i, byte[] bytes) {
                writeLine("Found device: " + bluetoothDevice.getAddress());
                // Check if the device has the UART service.
                if (parseUUIDs(bytes).contains(UART_UUID)) {
                    // Found a device, stop the scan.
                    adapter.stopLeScan(scanCallback);
                    writeLine("Found UART service!");
                    // Connect to the device.
                    // Control flow will now go to the callback functions when BTLE events occur.
                    gatt = bluetoothDevice.connectGatt(mContext, false, callback);     //changes made here
                }
            }
    };

   public void startBluetooth()
   {

       adapter.startLeScan(scanCallback);

   }

    private void writeLine(String text)
    {
        Log.d("message",text) ;
        // Toast.makeText(this,text,Toast.LENGTH_SHORT).show();
    }

    private List<UUID> parseUUIDs(final byte[] advertisedData) {
        List<UUID> uuids = new ArrayList<UUID>();

        int offset = 0;
        while (offset < (advertisedData.length - 2)) {
            int len = advertisedData[offset++];
            if (len == 0)
                break;

            int type = advertisedData[offset++];
            switch (type) {
                case 0x02: // Partial list of 16-bit UUIDs
                case 0x03: // Complete list of 16-bit UUIDs
                    while (len > 1) {
                        int uuid16 = advertisedData[offset++];
                        uuid16 += (advertisedData[offset++] << 8);
                        len -= 2;
                        uuids.add(UUID.fromString(String.format("%08x-0000-1000-8000-00805f9b34fb", uuid16)));
                    }
                    break;
                case 0x06:// Partial list of 128-bit UUIDs
                case 0x07:// Complete list of 128-bit UUIDs
                    // Loop through the advertised 128-bit UUID's.
                    while (len >= 16) {
                        try {
                            // Wrap the advertised bits and order them.
                            ByteBuffer buffer = ByteBuffer.wrap(advertisedData, offset++, 16).order(ByteOrder.LITTLE_ENDIAN);
                            long mostSignificantBit = buffer.getLong();
                            long leastSignificantBit = buffer.getLong();
                            uuids.add(new UUID(leastSignificantBit,
                                    mostSignificantBit));
                        } catch (IndexOutOfBoundsException e) {
                            // Defensive programming.
                            //Log.e(LOG_TAG, e.toString());
                            continue;
                        } finally {
                            // Move the offset to read the next uuid.
                            offset += 15;
                            len -= 16;
                        }
                    }
                    break;
                default:
                    offset += (len - 1);
                    break;
            }
        }
        return uuids;
    }


    private void setupFolderAndFile() {

        File folder = new File(Environment.getExternalStorageDirectory()
                + File.separator + AppConstants.APP_LOG_FOLDER_NAME);

        if (!folder.exists()) {
            folder.mkdirs();
        }

        mLogFile = new File(Environment.getExternalStorageDirectory().toString()
                + File.separator + AppConstants.APP_LOG_FOLDER_NAME
                + File.separator + "testBLE.txt");

        if (!mLogFile.exists()) {
            try {
                mLogFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (mFileStream == null) {
            try {
                mFileStream = new FileOutputStream(mLogFile, true);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }


    private void writeToFile(String formatted)
    {

        if (mFileStream != null && mLogFile.exists()) {

            try {
                mFileStream.write(formatted.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private void shutdownLogger() {
        //Flush and close file stream
        if (mFileStream != null) {
            try {
                mFileStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                mFileStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

}




