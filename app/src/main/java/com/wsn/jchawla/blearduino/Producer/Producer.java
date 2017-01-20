package com.wsn.jchawla.blearduino.Producer;

import com.wsn.jchawla.blearduino.Item.Item;

/**
 * Created by jchawla on 16.01.2017.
 */

public interface Producer {

    public Item startProducing();

    public void stopProducing();

}
