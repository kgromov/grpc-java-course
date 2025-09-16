package com.vinsguru.stock.service;

import com.google.protobuf.Empty;
import com.vinsguru.stock.PriceUpdate;
import com.vinsguru.stock.StockPriceRequest;
import com.vinsguru.stock.StockPriceResponse;
import com.vinsguru.stock.StockServiceGrpc;
import com.vinsguru.stock.service.event.StockPriceEvent;
import io.grpc.Status;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@GrpcService
public class StockServiceImpl extends StockServiceGrpc.StockServiceImplBase {
    private static final Logger log = LoggerFactory.getLogger(com.vinsguru.stock.service.StockServiceImpl.class);

    private final Tickers tickers;

    private final Set<ServerCallStreamObserver<PriceUpdate>> set;

    public StockServiceImpl(Tickers tickers) {
        this.tickers = tickers;
        this.set = Collections.synchronizedSet(new HashSet<>());
    }

    public void getStockPrice(StockPriceRequest request, StreamObserver<StockPriceResponse> responseObserver) {
        this.tickers.getPrice(request.getTicker())
                .map(v -> StockPriceResponse.newBuilder().setTicker(request.getTicker()).setPrice(v).build())
                .ifPresentOrElse(v -> {
                            responseObserver.onNext(v);
                            responseObserver.onCompleted();
                        },
                        () -> responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(request.getTicker() + " is not valid").asRuntimeException())
                );
    }

    public void getPriceUpdates(Empty request, StreamObserver<PriceUpdate> responseObserver) {
        ServerCallStreamObserver<PriceUpdate> o = (ServerCallStreamObserver<PriceUpdate>) responseObserver;
        this.set.add(o);
        o.setOnCancelHandler(() -> cancel(o));
        o.setOnCloseHandler(() -> cancel(o));
    }

    @EventListener
    public void onApplicationEvent(StockPriceEvent event) {
        for (ServerCallStreamObserver<PriceUpdate> o : this.set)
            o.onNext(event.getPriceUpdate());
    }

    private void cancel(ServerCallStreamObserver<PriceUpdate> o) {
        log.info("price updates observer cancelled");
        this.set.remove(o);
    }
}
