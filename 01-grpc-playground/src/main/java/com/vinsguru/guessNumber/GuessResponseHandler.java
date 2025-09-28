package com.vinsguru.guessNumber;

import com.vinsguru.models.sec08.GuessResponse;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class GuessResponseHandler implements StreamObserver<GuessResponse> {
    private final CountDownLatch latch;
    private GuessResponse lastResponse;
    private boolean isCompleted;
    private Throwable throwable;

    public GuessResponseHandler() {
        this.latch = new CountDownLatch(1);
    }

    @Override
    public void onNext(GuessResponse guessResponse) {
        this.lastResponse = guessResponse;
    }

    @Override
    public void onError(Throwable throwable) {
        this.throwable = throwable;
        this.latch.countDown();
    }

    @Override
    public void onCompleted() {
        this.latch.countDown();
    }

    public GuessResponse getLastResponse() {
        return lastResponse;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public void await() {
        try {
            this.latch.await(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isCompleted() {
        return latch.getCount() == 0;
    }
}
