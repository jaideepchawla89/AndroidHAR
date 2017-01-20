package com.wsn.jchawla.blearduino.Item;



/**
 * Created by jchawla on 20.01.2017.
 */

public class SensorReading implements Item {

    private double timestamp;
    private double yaw;
    private double roll;
    private double pitch;
    private double a_x;
    private double a_y;
    private double a_z;

    public SensorReading(double timestamp, double yaw, double roll, double pitch, double a_x, double a_y, double a_z) {
        this.timestamp = timestamp;
        this.yaw = yaw;
        this.roll = roll;
        this.pitch = pitch;
        this.a_x = a_x;
        this.a_y = a_y;
        this.a_z = a_z;
    }

    public double getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(double timestamp) {
        this.timestamp = timestamp;
    }

    public double getYaw() {
        return yaw;
    }

    public void setYaw(double yaw) {
        this.yaw = yaw;
    }

    public double getRoll() {
        return roll;
    }

    public void setRoll(double roll) {
        this.roll = roll;
    }

    public double getPitch() {
        return pitch;
    }

    public void setPitch(double pitch) {
        this.pitch = pitch;
    }

    public double getA_x() {
        return a_x;
    }

    public void setA_x(double a_x) {
        this.a_x = a_x;
    }

    public double getA_y() {
        return a_y;
    }

    public void setA_y(double a_y) {
        this.a_y = a_y;
    }

    public double getA_z() {
        return a_z;
    }

    public void setA_z(double a_z) {
        this.a_z = a_z;
    }

    @Override
    public String process() {
       String final_row= Double.toString(timestamp)+
               Double.toString(yaw)+
               Double.toString(roll)+
               Double.toString(pitch)+
               Double.toString(a_x)+
               Double.toString(a_y)+
               Double.toString(a_z)

                ;
return final_row;
    }
}
