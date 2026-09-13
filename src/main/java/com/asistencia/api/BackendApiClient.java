package com.asistencia.api;

import java.io.IOException;

public interface BackendApiClient {
    BackendResponse execute(BackendRequest request) throws IOException, InterruptedException;
}
