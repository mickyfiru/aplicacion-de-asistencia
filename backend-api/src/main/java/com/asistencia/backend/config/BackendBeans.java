package com.asistencia.backend.config;

import com.asistencia.backend.dataconnect.DataConnectAttendanceDataGateway;
import com.asistencia.backend.dataconnect.DataConnectRestClient;
import com.asistencia.backend.dataconnect.GoogleAccessTokenProvider;
import com.asistencia.backend.security.FirebaseAdminTokenVerifier;
import com.asistencia.backend.security.TokenVerifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.http.HttpClient;
import java.time.Clock;
import java.time.Duration;

@Configuration
public class BackendBeans {
    @Bean
    Clock backendClock() {
        return Clock.systemUTC();
    }

    @Bean
    TokenVerifier tokenVerifier() {
        return new FirebaseAdminTokenVerifier();
    }

    @Bean
    DataConnectRestClient dataConnectRestClient() {
        return new DataConnectRestClient(
                DataConnectSettings.fromEnvironment(),
                new GoogleAccessTokenProvider(),
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build()
        );
    }

    @Bean
    DataConnectAttendanceDataGateway attendanceDataGateway(DataConnectRestClient client) {
        return new DataConnectAttendanceDataGateway(client);
    }
}
