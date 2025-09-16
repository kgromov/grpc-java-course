package com.vinsguru.stock.service;

import com.vinsguru.common.Ticker;
import com.vinsguru.stock.PriceUpdate;
import com.vinsguru.stock.service.event.StockPriceEvent;
import jakarta.annotation.PostConstruct;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class Tickers {
    private final ApplicationEventPublisher publisher;

    private final Map<Ticker, Integer> map;

    public Tickers(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
        this.map = new HashMap<>();
    }

    @PostConstruct
    private void init() {
        this.map.put(Ticker.APPLE, 100);
        this.map.put(Ticker.GOOGLE, 100);
        this.map.put(Ticker.AMAZON, 100);
        this.map.put(Ticker.MICROSOFT, 100);
    }

    public Optional<Integer> getPrice(Ticker stockSymbol) {
        return Optional.ofNullable(this.map.get(stockSymbol));
    }

    @Scheduled(fixedRate = 1000L)
    public void updatePrice() {
        this.map.keySet()
                .forEach(k -> {
                    this.map.computeIfPresent(k, (t, v) -> v + this.randomValue());
                    PriceUpdate stock = PriceUpdate.newBuilder().setTicker(k).setPrice(this.map.get(k)).build();
                    this.publisher.publishEvent(new StockPriceEvent(this, stock));
                });
    }

    private int randomValue() {
        return ThreadLocalRandom.current().nextInt(-3, 4);
    }
}
