package com.wsn.jchawla.blearduino.ConsumerImplementation;

import android.os.Environment;

import com.wsn.jchawla.blearduino.Main.AppConstants;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by jchawla on 21.10.2015.
 *
 * Will most probably look to junk this class since it is not required
 */
public class dataConsumer implements Runnable {
    public static File mLogFile = null;
    public static FileOutputStream mFileStream = null;
     LinkedBlockingQueue<String> buffer=new LinkedBlockingQueue<>();
   private String output;


    public dataConsumer(LinkedBlockingQueue buf){

        this.buffer =buf;
        setupFolderAndFile();

    }


    @Override
    public void run() {


        while(true) {
            if (!buffer.isEmpty()) {
                try {
                    output = buffer.take();
                    writeToFile(output);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private void setupFolderAndFile() {

        File folder = new File(Environment.getExternalStorageDirectory()
                + File.separator + AppConstants.APP_LOG_FOLDER_NAME);

        if (!folder.exists()) {
            folder.mkdirs();
        }

        mLogFile = new File(Environment.getExternalStorageDirectory().toString()
                + File.separator + AppConstants.APP_LOG_FOLDER_NAME
                + File.separator + "testphone.txt");

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

 public void cleanThread()
 {

     shutdownLogger();
 }
}
