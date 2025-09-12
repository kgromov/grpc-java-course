package com.vinsguru.test.sec09;

import com.vinsguru.common.GrpcServer;
import com.vinsguru.models.sec09.BankServiceGrpc;
import com.vinsguru.sec09.BankService;
import com.vinsguru.test.common.AbstractChannelTest;
import io.grpc.Status;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.util.Optional;

public abstract class AbstractTest extends AbstractChannelTest {

    private final GrpcServer grpcServer = GrpcServer.create(new BankService());
    protected BankServiceGrpc.BankServiceStub bankStub;
    protected BankServiceGrpc.BankServiceBlockingStub bankBlockingStub;

    @BeforeAll
    public void setup(){
        this.grpcServer.start();
        this.bankStub = BankServiceGrpc.newStub(channel);
        this.bankBlockingStub = BankServiceGrpc.newBlockingStub(channel);
    }

    @AfterAll
    public void stop(){
        this.grpcServer.stop();
    }


    protected Status.Code  extractStatusCode(Throwable throwable) {
        return Optional.ofNullable(Status.fromThrowable(throwable))
                .map(Status::getCode)
                .orElseThrow();
    }
}
