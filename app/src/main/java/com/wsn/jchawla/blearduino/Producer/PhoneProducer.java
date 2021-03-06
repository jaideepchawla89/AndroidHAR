package com.wsn.jchawla.blearduino.Producer;

/**
 * Created by jchawla on 26.09.2015.
 */

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.util.Log;

import com.wsn.jchawla.blearduino.Item.Item;
import com.wsn.jchawla.blearduino.Item.SensorReading;
import com.wsn.jchawla.blearduino.Main.AppConstants;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by jchawla on 31.08.2015.
 */
public class PhoneProducer implements SensorEventListener,Runnable {

    private SensorManager mSensorManager=null;
    private Sensor accSensor=null;
    private Sensor gyroSensor=null;
    private Sensor magSensor=null;
   // private LinkedBlockingQueue<String> buffer=new LinkedBlockingQueue<>();
    private Context mContext;
    Item item ;

    public static File mLogFile = null;
    public static FileOutputStream mFileStream = null;

    private String pName;
    private String activityname;


    // angular speeds from gyro
    private float[] gyro = new float[3];

    // rotation matrix from gyro data
    private float[] gyroMatrix = new float[9];

    // orientation angles from gyro matrix
    private float[] gyroOrientation = new float[3];

    // magnetic field vector
    private float[] magnet = new float[3];

    // accelerometer vector
    private float[] accel = new float[3];

    // orientation angles from accel and magnet
    private float[] accMagOrientation = new float[3];

    // final orientation angles from sensor fusion
    private float[] fusedOrientation = new float[3];

    private float[] selectedOrientation = fusedOrientation;



    public enum Mode {
        ACC_MAG, GYRO, FUSION
    }

    // accelerometer and magnetometer based rotation matrix
    private float[] rotationMatrix = new float[9];

    public static final float EPSILON = 0.000000001f;
    private static final float NS2S = 1.0f / 1000000000.0f;
    private long timestamp;
    private boolean initState = true;

    public static final int TIME_CONSTANT = 30;   //probably needs to be changed acc to sensor refresh rate
    public static final float FILTER_COEFFICIENT = 0.98f;
    private Timer fuseTimer = new Timer();








    public PhoneProducer(Context context,  String name, String aName)
    {
        mContext =context;
        //this.buffer = buf;

        gyroOrientation[0] = 0.0f;
        gyroOrientation[1] = 0.0f;
        gyroOrientation[2] = 0.0f;

        //setupFolderAndFile(); //set up folder and file

        // initialise gyroMatrix with identity matrix
        gyroMatrix[0] = 1.0f;
        gyroMatrix[1] = 0.0f;
        gyroMatrix[2] = 0.0f;
        gyroMatrix[3] = 0.0f;
        gyroMatrix[4] = 1.0f;
        gyroMatrix[5] = 0.0f;
        gyroMatrix[6] = 0.0f;
        gyroMatrix[7] = 0.0f;
        gyroMatrix[8] = 1.0f;

        // wait for one second until gyroscope and magnetometer/accelerometer
        // data is initialised then schedule the complementary filter task
        // fuseTimer.scheduleAtFixedRate(new calculateFusedOrientationTask(), 1000, TIME_CONSTANT);
        pName=name;
        activityname=aName;
        Log.d("service", mContext.toString());
        mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);

        accSensor =mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroSensor =mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        magSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }



    @Override
    public void run() {
        //handler.sendMessage();


        mSensorManager.registerListener(this, accSensor,200000 ,200000);
        mSensorManager.registerListener(this,gyroSensor,200000 ,200000);
        mSensorManager.registerListener(this, magSensor, 200000 ,200000);
        fuseTimer.scheduleAtFixedRate(new calculateFusedOrientationTask(), 1000, TIME_CONSTANT);


        Log.d("in run()", "Run()method");


    }

    private void getAccelerometer(SensorEvent event)  {

        // Log.d("in thread", "getAccel");



        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                setAccel(event.values);
                calculateAccMagOrientation();
                break;

            case Sensor.TYPE_GYROSCOPE:
                gyroFunction(event);
                break;

            case Sensor.TYPE_MAGNETIC_FIELD:
                setMagnet(event.values);
                break;
        }

        double azimuthValue = getAzimuth();
        double rollValue =  getRoll();
        double pitchValue =  getPitch();
        item=new SensorReading(event.timestamp/1000,azimuthValue,rollValue,pitchValue,Double.valueOf(accel[0]),Double.valueOf(accel[1]),Double.valueOf(accel[2])) ;


        //  Log.d("azimuth",Double.toString(azimuthValue));
        //  Log.d("roll",Double.toString(rollValue));
        //  Log.d("pitch", Double.toString(pitchValue));
     /*   String formatted = String.valueOf(System.currentTimeMillis()/1000)
                // + "," + String.valueOf(event.timestamp/1000)
                +"," + pName
                +","+ activityname
                + "," + String.valueOf(azimuthValue)
                + "," + String.valueOf(rollValue)
                + "," + String.valueOf(pitchValue)
                + "," + String.valueOf(accel[0])
                + "," + String.valueOf(accel[1])
                + "," + String.valueOf(accel[2])
                + "," + "p";


        try {
            buffer.put(formatted);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //   writeToFile(formatted);
        // Log.d("in phone",formatted);
        return formatted;*/


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // do nothing
    }




    @Override
    public void onSensorChanged(SensorEvent event) {
        // do nothing
        getAccelerometer(event);
    }

    public void cleanThread(){

        if(mSensorManager != null) {
            mSensorManager.unregisterListener(this);
            Log.d("in thread", "cleanThread");
        }
        fuseTimer.cancel();
        //shutdownLogger();
    }












    public double getAzimuth() {
        return selectedOrientation[0] * 180 / Math.PI;
    }

    public double getPitch() {
        return selectedOrientation[1] * 180 / Math.PI;
    }

    public double getRoll() {
        return selectedOrientation[2] * 180 / Math.PI;
    }

    public void setMagnet(float[] sensorValues) {
        System.arraycopy(sensorValues, 0, magnet, 0, 3);
    }

    public void setAccel(float[] sensorValues) {
        System.arraycopy(sensorValues, 0, accel, 0, 3);

    }
    public  void setMode(Mode mode) {
        Log.i("tag", "msg 0"+ mode);
        Log.i("tag", "msg 00000" + Mode.ACC_MAG);
        switch (mode) {
            case ACC_MAG:
                Log.i("tag", "msg 1"+ mode);
                selectedOrientation = accMagOrientation;
                break;
            case GYRO:
                Log.i("tag", "msg 2"+ mode);
                selectedOrientation = gyroOrientation;
                break;
            case FUSION:
                Log.i("tag", "msg 3"+ mode);
                selectedOrientation = fusedOrientation;
                break;
            default:
                Log.i("tag", "msg 4"+ mode);
                selectedOrientation = fusedOrientation;
                break;
        }
    }
    public void calculateAccMagOrientation() {
        if (SensorManager.getRotationMatrix(rotationMatrix, null, accel, magnet)) {
            SensorManager.getOrientation(rotationMatrix, accMagOrientation);
        }
    }

    private void getRotationVectorFromGyro(float[] gyroValues,
                                           float[] deltaRotationVector,
                                           float timeFactor) {
        float[] normValues = new float[3];

        // Calculate the angular speed of the sample
        float omegaMagnitude =
                (float) Math.sqrt(gyroValues[0] * gyroValues[0] +
                        gyroValues[1] * gyroValues[1] +
                        gyroValues[2] * gyroValues[2]);

        // Normalize the rotation vector if it's big enough to get the axis
        if (omegaMagnitude > EPSILON) {
            normValues[0] = gyroValues[0] / omegaMagnitude;
            normValues[1] = gyroValues[1] / omegaMagnitude;
            normValues[2] = gyroValues[2] / omegaMagnitude;
        }

        // Integrate around this axis with the angular speed by the timestep
        // in order to get a delta rotation from this sample over the timestep
        // We will convert this axis-angle representation of the delta rotation
        // into a quaternion before turning it into the rotation matrix.
        float thetaOverTwo = omegaMagnitude * timeFactor;
        float sinThetaOverTwo = (float) Math.sin(thetaOverTwo);
        float cosThetaOverTwo = (float) Math.cos(thetaOverTwo);
        deltaRotationVector[0] = sinThetaOverTwo * normValues[0];
        deltaRotationVector[1] = sinThetaOverTwo * normValues[1];
        deltaRotationVector[2] = sinThetaOverTwo * normValues[2];
        deltaRotationVector[3] = cosThetaOverTwo;
    }


    // This function performs the integration of the gyroscope data.
    // It writes the gyroscope based orientation into gyroOrientation.
    public void gyroFunction(SensorEvent event) {
        // don't start until first accelerometer/magnetometer orientation has been acquired
        if (accMagOrientation == null)
            return;

        // initialisation of the gyroscope based rotation matrix
        if (initState) {
            float[] initMatrix = new float[9];
            initMatrix = getRotationMatrixFromOrientation(accMagOrientation);
            float[] test = new float[3];
            SensorManager.getOrientation(initMatrix, test);
            gyroMatrix = matrixMultiplication(gyroMatrix, initMatrix);
            initState = false;
        }

        // copy the new gyro values into the gyro array
        // convert the raw gyro data into a rotation vector
        float[] deltaVector = new float[4];
        if (timestamp != 0) {
            final float dT = (event.timestamp - timestamp) * NS2S;
            System.arraycopy(event.values, 0, gyro, 0, 3);
            getRotationVectorFromGyro(gyro, deltaVector, dT / 2.0f);
        }

        // measurement done, save current time for next interval
        timestamp = event.timestamp;

        // convert rotation vector into rotation matrix
        float[] deltaMatrix = new float[9];
        SensorManager.getRotationMatrixFromVector(deltaMatrix, deltaVector);

        // apply the new rotation interval on the gyroscope based rotation matrix
        gyroMatrix = matrixMultiplication(gyroMatrix, deltaMatrix);

        // get the gyroscope based orientation from the rotation matrix
        SensorManager.getOrientation(gyroMatrix, gyroOrientation);
    }

    private float[] getRotationMatrixFromOrientation(float[] o) {
        float[] xM = new float[9];
        float[] yM = new float[9];
        float[] zM = new float[9];

        float sinX = (float) Math.sin(o[1]);
        float cosX = (float) Math.cos(o[1]);
        float sinY = (float) Math.sin(o[2]);
        float cosY = (float) Math.cos(o[2]);
        float sinZ = (float) Math.sin(o[0]);
        float cosZ = (float) Math.cos(o[0]);

        // rotation about x-axis (pitch)
        xM[0] = 1.0f;
        xM[1] = 0.0f;
        xM[2] = 0.0f;
        xM[3] = 0.0f;
        xM[4] = cosX;
        xM[5] = sinX;
        xM[6] = 0.0f;
        xM[7] = -sinX;
        xM[8] = cosX;

        // rotation about y-axis (roll)
        yM[0] = cosY;
        yM[1] = 0.0f;
        yM[2] = sinY;
        yM[3] = 0.0f;
        yM[4] = 1.0f;
        yM[5] = 0.0f;
        yM[6] = -sinY;
        yM[7] = 0.0f;
        yM[8] = cosY;

        // rotation about z-axis (azimuth)
        zM[0] = cosZ;
        zM[1] = sinZ;
        zM[2] = 0.0f;
        zM[3] = -sinZ;
        zM[4] = cosZ;
        zM[5] = 0.0f;
        zM[6] = 0.0f;
        zM[7] = 0.0f;
        zM[8] = 1.0f;

        // rotation order is y, x, z (roll, pitch, azimuth)
        float[] resultMatrix = matrixMultiplication(xM, yM);
        resultMatrix = matrixMultiplication(zM, resultMatrix);
        return resultMatrix;
    }

    private float[] matrixMultiplication(float[] A, float[] B) {
        float[] result = new float[9];

        result[0] = A[0] * B[0] + A[1] * B[3] + A[2] * B[6];
        result[1] = A[0] * B[1] + A[1] * B[4] + A[2] * B[7];
        result[2] = A[0] * B[2] + A[1] * B[5] + A[2] * B[8];

        result[3] = A[3] * B[0] + A[4] * B[3] + A[5] * B[6];
        result[4] = A[3] * B[1] + A[4] * B[4] + A[5] * B[7];
        result[5] = A[3] * B[2] + A[4] * B[5] + A[5] * B[8];

        result[6] = A[6] * B[0] + A[7] * B[3] + A[8] * B[6];
        result[7] = A[6] * B[1] + A[7] * B[4] + A[8] * B[7];
        result[8] = A[6] * B[2] + A[7] * B[5] + A[8] * B[8];

        return result;
    }

    class calculateFusedOrientationTask extends TimerTask {
        public void run() {
            float oneMinusCoeff = 1.0f - FILTER_COEFFICIENT;

            /*
             * Fix for 179∞ <--> -179∞ transition problem:
             * Check whether one of the two orientation angles (gyro or accMag) is negative while the other one is positive.
             * If so, add 360∞ (2 * math.PI) to the negative value, perform the sensor fusion, and remove the 360∞ from the result
             * if it is greater than 180∞. This stabilizes the output in positive-to-negative-transition cases.
             */

            // azimuth
            if (gyroOrientation[0] < -0.5 * Math.PI && accMagOrientation[0] > 0.0) {
                fusedOrientation[0] = (float) (FILTER_COEFFICIENT * (gyroOrientation[0] + 2.0 * Math.PI) + oneMinusCoeff * accMagOrientation[0]);
                fusedOrientation[0] -= (fusedOrientation[0] > Math.PI) ? 2.0 * Math.PI : 0;
            } else if (accMagOrientation[0] < -0.5 * Math.PI && gyroOrientation[0] > 0.0) {
                fusedOrientation[0] = (float) (FILTER_COEFFICIENT * gyroOrientation[0] + oneMinusCoeff * (accMagOrientation[0] + 2.0 * Math.PI));
                fusedOrientation[0] -= (fusedOrientation[0] > Math.PI) ? 2.0 * Math.PI : 0;
            } else {
                fusedOrientation[0] = FILTER_COEFFICIENT * gyroOrientation[0] + oneMinusCoeff * accMagOrientation[0];
            }

            // pitch
            if (gyroOrientation[1] < -0.5 * Math.PI && accMagOrientation[1] > 0.0) {
                fusedOrientation[1] = (float) (FILTER_COEFFICIENT * (gyroOrientation[1] + 2.0 * Math.PI) + oneMinusCoeff * accMagOrientation[1]);
                fusedOrientation[1] -= (fusedOrientation[1] > Math.PI) ? 2.0 * Math.PI : 0;
            } else if (accMagOrientation[1] < -0.5 * Math.PI && gyroOrientation[1] > 0.0) {
                fusedOrientation[1] = (float) (FILTER_COEFFICIENT * gyroOrientation[1] + oneMinusCoeff * (accMagOrientation[1] + 2.0 * Math.PI));
                fusedOrientation[1] -= (fusedOrientation[1] > Math.PI) ? 2.0 * Math.PI : 0;
            } else {
                fusedOrientation[1] = FILTER_COEFFICIENT * gyroOrientation[1] + oneMinusCoeff * accMagOrientation[1];
            }

            // roll
            if (gyroOrientation[2] < -0.5 * Math.PI && accMagOrientation[2] > 0.0) {
                fusedOrientation[2] = (float) (FILTER_COEFFICIENT * (gyroOrientation[2] + 2.0 * Math.PI) + oneMinusCoeff * accMagOrientation[2]);
                fusedOrientation[2] -= (fusedOrientation[2] > Math.PI) ? 2.0 * Math.PI : 0;
            } else if (accMagOrientation[2] < -0.5 * Math.PI && gyroOrientation[2] > 0.0) {
                fusedOrientation[2] = (float) (FILTER_COEFFICIENT * gyroOrientation[2] + oneMinusCoeff * (accMagOrientation[2] + 2.0 * Math.PI));
                fusedOrientation[2] -= (fusedOrientation[2] > Math.PI) ? 2.0 * Math.PI : 0;
            } else {
                fusedOrientation[2] = FILTER_COEFFICIENT * gyroOrientation[2] + oneMinusCoeff * accMagOrientation[2];
            }

            // overwrite gyro matrix and orientation with fused orientation
            // to comensate gyro drift
            gyroMatrix = getRotationMatrixFromOrientation(fusedOrientation);
            System.arraycopy(fusedOrientation, 0, gyroOrientation, 0, 3);


            // update sensor output in GUI
            //mainHandler.post(updateOreintationDisplayTask);
        }
    }







}