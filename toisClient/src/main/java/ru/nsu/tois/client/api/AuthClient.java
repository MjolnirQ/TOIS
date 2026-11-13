package ru.nsu.tois.client.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import ru.nsu.tois.client.api.dto.request.LoginRequest;
import ru.nsu.tois.client.api.dto.response.LoginResponse;

import java.net.http.HttpClient;

public class AuthClient extends BaseHttpClient{
    public AuthClient(
            String baseUrl,
            HttpClient httpClient,
            ObjectMapper objectMapper
    ) {
        super(baseUrl, httpClient, objectMapper);
    }

    public LoginResponse login(String username, String password) {
        LoginRequest request = new LoginRequest(username, password);

        return postAuth(
                "/auth/login",
                request,
                LoginResponse.class
        );
    }
}
