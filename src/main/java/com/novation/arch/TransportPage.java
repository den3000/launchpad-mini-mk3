package com.novation.arch;

import com.novation.hal.IHal;

public class TransportPage implements ITransportHal.Listener {
    PageType type;
    private final ITransportHal hal;

    public TransportPage(ITransportHal hal) {
        this.type = PageType.custom;
        this.hal = hal;

        hal.setListener(this);
    }
}
