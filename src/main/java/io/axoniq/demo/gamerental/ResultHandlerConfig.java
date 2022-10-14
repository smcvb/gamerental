package io.axoniq.demo.gamerental;

import org.axonframework.extensions.reactor.commandhandling.gateway.ReactorCommandGateway;
import org.axonframework.extensions.reactor.queryhandling.gateway.ReactorQueryGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ResultHandlerConfig {

    @Autowired
    public void configureResultHandlerInterceptors(ReactorCommandGateway commandGateway,
                                                   ReactorQueryGateway queryGateway) {
        commandGateway.registerResultHandlerInterceptor(
                (cmd, result) -> result.onErrorMap(ExceptionMapper::mapRemoteException)
        );

        queryGateway.registerResultHandlerInterceptor(
                (query, result) -> result.onErrorMap(ExceptionMapper::mapRemoteException)
        );
    }
}
