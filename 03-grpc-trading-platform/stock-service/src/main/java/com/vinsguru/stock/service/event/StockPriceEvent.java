package com.vinsguru.stock.service.event;

import com.vinsguru.stock.PriceUpdate;

public class StockPriceEvent {
    private final PriceUpdate priceUpdate;

    public StockPriceEvent(Object source, PriceUpdate priceUpdate) {
        this.priceUpdate = priceUpdate;
    }

    public PriceUpdate getPriceUpdate() {
        return this.priceUpdate;
    }
}
