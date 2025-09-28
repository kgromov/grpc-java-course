package com.vinsguru.guessNumber;

import com.vinsguru.models.sec08.GuessNumberGrpc;
import com.vinsguru.models.sec08.GuessRequest;
import com.vinsguru.models.sec08.GuessResponse;
import com.vinsguru.models.sec08.Result;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.ThreadLocalRandom;

public class GuessNumberService extends GuessNumberGrpc.GuessNumberImplBase {

    @Override
    public StreamObserver<GuessRequest> makeGuess(StreamObserver<GuessResponse> responseObserver) {
        return new GuessRequestHandler(responseObserver);
    }

    private static class GuessRequestHandler implements StreamObserver<GuessRequest> {

        private final StreamObserver<GuessResponse> responseObserver;
        private final int secret;
        private int attempt;

        public GuessRequestHandler(StreamObserver<GuessResponse> responseObserver) {
            this.responseObserver = responseObserver;
            this.attempt = 0;
            this.secret = ThreadLocalRandom.current().nextInt(1, 101);
        }

        @Override
        public void onNext(GuessRequest guessRequest) {
            if (guessRequest.getGuess() == secret) {
                System.out.printf("client guess %d is correct%n", guessRequest.getGuess());
                System.out.printf("Win after %d attempts%n", (++attempt));
                this.responseObserver.onCompleted();
            } else if (guessRequest.getGuess() > secret) {
                this.send(Result.TOO_HIGH);
            } else {
                this.send(Result.TOO_LOW);
            }
        }

        @Override
        public void onError(Throwable throwable) {

        }

        @Override
        public void onCompleted() {
            this.responseObserver.onCompleted();
        }

        private void send(Result result) {
            attempt++;
            var response = GuessResponse.newBuilder()
                    .setAttempt(attempt)
                    .setResult(result)
                    .build();
            System.out.printf("client guess is %s%nNext (%d) attempt%n", result, attempt + 1);
            this.responseObserver.onNext(response);
        }
    }

}
