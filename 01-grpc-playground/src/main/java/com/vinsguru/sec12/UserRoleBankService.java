package com.vinsguru.sec12;

import com.vinsguru.models.sec12.AccountBalance;
import com.vinsguru.models.sec12.BalanceCheckRequest;
import com.vinsguru.models.sec12.BankServiceGrpc;
import com.vinsguru.sec12.repository.AccountRepository;
import io.grpc.stub.StreamObserver;

public class UserRoleBankService extends BankServiceGrpc.BankServiceImplBase {

    @Override
    public void getAccountBalance(BalanceCheckRequest request, StreamObserver<AccountBalance> responseObserver) {
        var accountNumber = request.getAccountNumber();
        var balance = AccountRepository.getBalance(accountNumber);
        if (UserRole.STANDARD.equals(Constants.USER_ROLE_KEY.get())) {
            var fee = balance > 0 ? 1 : 0;
            AccountRepository.deductAmount(accountNumber, fee);
            balance = balance - fee;
        }
        var accountBalance = AccountBalance.newBuilder()
                            .setAccountNumber(accountNumber)
                            .setBalance(balance)
                            .build();
        responseObserver.onNext(accountBalance);
        responseObserver.onCompleted();
    }

}
