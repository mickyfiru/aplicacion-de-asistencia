package com.asistencia.api;

import java.net.URI;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;

public class BackendRequest {
    private final String operationName;
    private final URI uri;
    private final String method;
    private final String body;
    private final Map<String, String> headers;
    private final Duration timeout;

    public BackendRequest(
            String operationName,
            URI uri,
            String method,
            String body,
            Map<String, String> headers,
            Duration timeout
    ) {
        this.operationName = operationName;
        this.uri = uri;
        this.method = method;
        this.body = body;
        this.headers = headers == null ? Map.of() : Collections.unmodifiableMap(headers);
        this.timeout = timeout == null ? Duration.ofSeconds(10) : timeout;
    }

    public String getOperationName() {
        return operationName;
    }

    public URI getUri() {
        return uri;
    }

    public String getMethod() {
        return method;
    }

    public String getBody() {
        return body;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public Duration getTimeout() {
        return timeout;
    }
}
