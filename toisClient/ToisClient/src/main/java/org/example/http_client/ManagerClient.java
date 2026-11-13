package org.example.http_client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.StringJoiner;

public class ManagerClient extends BaseHttpClient {
    public ManagerClient(
            String baseUrl,
            HttpClient httpClient,
            ObjectMapper objectMapper
    ) {
        super(baseUrl, httpClient, objectMapper);
    }

    public Object execute(
            String token,
            String method,
            String path,
            Map<String, Object> queryParams,
            Map<String, Object> body
    ) {
        String requestPath = appendQueryParams(path, queryParams);

        return switch (method) {
            case "GET" -> get(requestPath, token, new TypeReference<Object>() {
            });
            case "POST" -> post(requestPath, token, body, Object.class);
            case "PUT" -> put(requestPath, token, body, Object.class);
            case "PATCH" -> patch(requestPath, token, body, Object.class);
            case "DELETE" -> {
                delete(requestPath, token);
                yield Map.of("status", "deleted");
            }
            default -> throw new IllegalArgumentException("Unsupported method: " + method);
        };
    }

    private String appendQueryParams(String path, Map<String, Object> queryParams) {
        if (queryParams == null || queryParams.isEmpty()) {
            return path;
        }

        StringJoiner joiner = new StringJoiner("&");
        queryParams.forEach((key, value) -> {
            if (value != null) {
                joiner.add(encode(key) + "=" + encode(String.valueOf(value)));
            }
        });

        String query = joiner.toString();
        if (query.isBlank()) {
            return path;
        }
        return path + (path.contains("?") ? "&" : "?") + query;
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
