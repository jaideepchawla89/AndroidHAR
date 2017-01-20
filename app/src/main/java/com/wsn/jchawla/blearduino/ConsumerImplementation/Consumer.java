package com.wsn.jchawla.blearduino.ConsumerImplementation;

import com.wsn.jchawla.blearduino.Item.Item;

/**
 * Created by jchawla on 24.10.2015.
 */
public interface Consumer {

    public boolean consume(Item j);
    public void finishConsumption();
}
