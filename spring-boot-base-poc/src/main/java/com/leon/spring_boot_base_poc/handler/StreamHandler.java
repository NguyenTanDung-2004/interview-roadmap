package com.leon.spring_boot_base_poc.handler;

import com.amazonaws.serverless.exceptions.ContainerInitializationException;
import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.serverless.proxy.model.HttpApiV2ProxyRequest;
import com.amazonaws.serverless.proxy.spring.SpringBootLambdaContainerHandler;
import com.amazonaws.serverless.proxy.spring.SpringBootProxyHandlerBuilder;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leon.spring_boot_base_poc.SpringBootBasePocApplication;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StreamHandler implements RequestStreamHandler {

    // v1 handler: REST API (proxy integration)
    private static final SpringBootLambdaContainerHandler<AwsProxyRequest, AwsProxyResponse> v1Handler;
    // v2 handler: HTTP API (payload format 2.0)
    private static final SpringBootLambdaContainerHandler<HttpApiV2ProxyRequest, AwsProxyResponse> v2Handler;

    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        try {
            v1Handler = SpringBootLambdaContainerHandler.getAwsProxyHandler(SpringBootBasePocApplication.class);
            v2Handler = new SpringBootProxyHandlerBuilder<HttpApiV2ProxyRequest>()
                    .defaultHttpApiV2Proxy()
                    .springBootApplication(SpringBootBasePocApplication.class)
                    .buildAndInitialize();
        } catch (ContainerInitializationException e) {
            throw new RuntimeException("Could not initialize Spring Boot application", e);
        }
    }

    @Override
    public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {
        // Buffer the input so we can inspect it and replay it
        byte[] bytes = input.readAllBytes();
        JsonNode root = mapper.readTree(bytes);

        // HTTP API v2 events have a "version" field set to "2.0"
        JsonNode version = root.get("version");
        if (version != null && "2.0".equals(version.asText())) {
            v2Handler.proxyStream(new ByteArrayInputStream(bytes), output, context);
        } else {
            v1Handler.proxyStream(new ByteArrayInputStream(bytes), output, context);
        }
    }
}