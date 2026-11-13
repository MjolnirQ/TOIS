package org.example.model;

public class SessionState {
    private Role selectedRole;
    private String token;
    public static final String BASE_URL = System.getenv()
            .getOrDefault("TOIS_API_URL", "http://localhost:8081/api");

    public void setRole(Role role) { this.selectedRole = role; }
    public void setToken(String token) { this.token = token; }
    public String getToken() { return token; }
}
