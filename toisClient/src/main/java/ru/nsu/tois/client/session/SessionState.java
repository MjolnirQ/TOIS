package ru.nsu.tois.client.session;

public class SessionState {
    private String token;
    public static final String BASE_URL = System.getenv()
            .getOrDefault("TOIS_API_URL", "http://localhost:8081/api");

    public void setToken(String token) { this.token = token; }
    public String getToken() { return token; }
}
