package com.vinsguru.guessNumber;

import com.vinsguru.common.GrpcServer;
import com.vinsguru.models.sec08.GuessNumberGrpc;
import com.vinsguru.models.sec08.GuessRequest;
import com.vinsguru.models.sec08.Result;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.Scanner;

public class ConsoleGame {

    public static void main(String[] args) {
        GrpcServer server = GrpcServer.create(new GuessNumberService());
        server.start();

        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 6565)
                .usePlaintext()
                .build();
        GuessNumberGrpc.GuessNumberStub stub = GuessNumberGrpc.newStub(channel);
        var responseObserver = new GuessResponseHandler();
        var requestObserver = stub.makeGuess(responseObserver);
        System.out.println("=== GUESS THE NUMBER ===");
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                String userMessage = scanner.nextLine();

                if ("exit".equalsIgnoreCase(userMessage)) {
                    System.out.println("You lost the game!");
                    requestObserver.onCompleted();
                    responseObserver.onCompleted();
                    break;
                }
                try {
                    int guess = Integer.parseInt(userMessage);
                    requestObserver.onNext(GuessRequest.newBuilder().setGuess(guess).build());
                    responseObserver.await();
                    if (responseObserver.isCompleted()) {
                        requestObserver.onCompleted();
                        break;
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Please enter a valid number between 1 and 100");
                }
            }
        }
        System.out.println("=== GAME OVER ===");
        server.stop();
    }
}
