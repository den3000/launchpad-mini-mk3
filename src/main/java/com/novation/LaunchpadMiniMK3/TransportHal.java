package com.novation.LaunchpadMiniMK3;

import com.bitwig.extension.controller.api.Transport;
import com.novation.arch.ITransportHal;

public class TransportHal implements ITransportHal {

    Listener listener;
    public TransportHal(Transport transport) {
//        transport.isPlaying().addValueObserver();
    }

    @Override
    public void setListener(Listener l) {
        listener = l;
    }
}
 