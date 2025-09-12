package com.vinsguru.test.sec12;

import com.vinsguru.sec12.Constants;
import io.grpc.CallCredentials;
import io.grpc.Metadata;

import java.util.concurrent.Executor;

class UserSessionToken extends CallCredentials {

    private static final String TOKEN_FORMAT = "%s %s";
    private final String jwt;

    public UserSessionToken(String jwt) {
        this.jwt = jwt;
    }

    @Override
    public void applyRequestMetadata(RequestInfo requestInfo, Executor executor, MetadataApplier metadataApplier) {
        executor.execute(() -> {
            var metadata = new Metadata();
            metadata.put(Constants.USER_TOKEN_KEY, TOKEN_FORMAT.formatted(Constants.BEARER, jwt));
            metadataApplier.apply(metadata);
        });
    }

}
