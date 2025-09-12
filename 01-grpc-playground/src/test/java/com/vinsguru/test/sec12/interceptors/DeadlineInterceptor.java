package com.vinsguru.test.sec12.interceptors;

import io.grpc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class DeadlineInterceptor implements ClientInterceptor {
    private static final Logger log = LoggerFactory.getLogger(DeadlineInterceptor.class);

    private final Duration duration;

    public DeadlineInterceptor(Duration duration) {
        this.duration = duration;
    }

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> methodDescriptor,
                                                               CallOptions callOptions,
                                                               Channel channel) {
        callOptions = Objects.nonNull(callOptions.getDeadline())
                ? callOptions
                : callOptions.withDeadline(Deadline.after(duration.toMillis(), TimeUnit.MILLISECONDS));
        log.debug("callOptions: deadline = {}, defaultDeadline = {}s", callOptions.getDeadline(), duration.getSeconds());
        return channel.newCall(methodDescriptor, callOptions);
    }

}
