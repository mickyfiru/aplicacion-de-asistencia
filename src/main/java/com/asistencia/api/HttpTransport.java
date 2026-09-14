package com.asistencia.api;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

public interface HttpTransport {
    HttpResponseData post(URI uri, Map<String, String> headers, String body) throws IOException, InterruptedException;
}
