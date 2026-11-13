package org.example.http_client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.http_client.DTOrequest.LoginRequest;
import org.example.http_client.DTOresponse.LoginResponse;

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
