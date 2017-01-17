package com.wsn.jchawla.blearduino.ConsumerImplementation;

/**
 * Created by jchawla on 24.10.2015.
 */
public interface Consumer {

    public boolean consume(String j);
    public void finishConsumption();
}
