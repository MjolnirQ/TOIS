package org.example.http_client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.exception.ApiException;
import org.example.http_client.DTOresponse.ErrorResponse;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class BaseHttpClient {
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String baseUrl;

    public BaseHttpClient(String baseUrl, HttpClient httpClient, ObjectMapper objectMapper) {
        this.baseUrl = baseUrl;
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    protected ObjectMapper objectMapper() {
        return objectMapper;
    }

    protected <T> T get(String path, String token, Class<T> responseType) {
        try {
            String body = sendGet(path, token);

            return objectMapper.readValue(body, responseType);

        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("HTTP GET failed: " + path, e);
        }
    }

    protected <T> T get(String path, String token, TypeReference<T> responseType) {
        try {
            String body = sendGet(path, token);

            return objectMapper.readValue(body, responseType);

        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("HTTP GET failed: " + path, e);
        }
    }

    protected <REQ, RES> RES postAuth(
            String authPath,
            REQ requestBody,
            Class<RES> responseType
    ) {
        try {
            HttpResponse<String> response = processPost(authPath, requestBody);

            checkStatus(response);

            return objectMapper.readValue(response.body(), responseType);

        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("HTTP POST failed: " + authPath, e);
        }
    }

    protected <REQ, RES> RES post(
            String path,
            String token,
            REQ requestBody,
            Class<RES> responseType
    ) {
        try {
            HttpResponse<String> response = processPost(path, token, requestBody);
            checkStatus(response);

            return objectMapper.readValue(response.body(), responseType);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("HTTP POST failed: " + path, e);
        }
    }

    protected <REQ, RES> RES put(
            String path,
            String token,
            REQ requestBody,
            Class<RES> responseType
    ) {
        try {
            String json = objectMapper.writeValueAsString(requestBody);
            HttpRequest request = buildRequestWithBody("PUT", path, json, token);
            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
            );
            checkStatus(response);

            return objectMapper.readValue(response.body(), responseType);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("HTTP PUT failed: " + path, e);
        }
    }

    protected <REQ, RES> RES patch(
            String path,
            String token,
            REQ requestBody,
            Class<RES> responseType
    ) {
        try {
            String json = objectMapper.writeValueAsString(requestBody);
            HttpRequest request = buildRequestWithBody("PATCH", path, json, token);
            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
            );
            checkStatus(response);

            return objectMapper.readValue(response.body(), responseType);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("HTTP PATCH failed: " + path, e);
        }
    }

    protected void delete(String path, String token) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + path))
                    .header("Authorization", "Bearer " + token)
                    .header("Accept", "application/json")
                    .DELETE()
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
            );
            checkStatus(response);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("HTTP DELETE failed: " + path, e);
        }
    }

    private void checkStatus(HttpResponse<String> response) throws Exception {
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            ErrorResponse errorResponse = objectMapper.readValue(response.body(), ErrorResponse.class);

            throw new ApiException(response.statusCode(), errorResponse);
        }
    }

    protected <REQ> HttpResponse<String> processPost(String path, String token, REQ requestBody) {
        try {
            String json = objectMapper.writeValueAsString(requestBody);

            HttpRequest request = buildPostRequest(path, json, token);

            return httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
            );
        } catch (Exception e) {
            throw new RuntimeException("HTTP POST failed: " + path, e);
        }
    }

    protected <REQ> HttpResponse<String> processPost(String path, REQ requestBody) {
        try {
            String json = objectMapper.writeValueAsString(requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + path))
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json; charset=UTF-8")
                    .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                    .build();

            return httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
            );
        } catch (Exception e) {
            throw new RuntimeException("HTTP POST failed: " + path, e);
        }
    }

    private String sendGet(String path, String token) throws Exception {

        HttpRequest request = buildGetRequest(path, token);

        HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
        );

        checkStatus(response);

        return response.body();
    }

    private HttpRequest buildGetRequest(String path, String token) throws Exception {
        return HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .header("Authorization", "Bearer " + token)
                .header("Accept", "application/json")
                .GET()
                .build();
    }

    private HttpRequest buildPostRequest(String path, String body, String token) throws Exception {
        return buildRequestWithBody("POST", path, body, token);
    }

    private HttpRequest buildRequestWithBody(String method, String path, String body, String token) throws Exception {
        return HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .header("Authorization", "Bearer " + token)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json; charset=UTF-8")
                .method(method, HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();
    }
}
