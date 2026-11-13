package ru.nsu.tois.client.session;

import com.fasterxml.jackson.databind.ObjectMapper;
import ru.nsu.tois.client.controller.WindowManager;
import ru.nsu.tois.client.api.AdminClient;
import ru.nsu.tois.client.api.AuthClient;
import ru.nsu.tois.client.api.ManagerClient;
import ru.nsu.tois.client.api.SellerClient;

import java.net.http.HttpClient;

public class AppContext {
    private final SessionState model;
    private final WindowManager windowManager;


    private final AuthClient authClient;
    private final AdminClient adminClient;
    private final SellerClient sellerClient;
    private final ManagerClient managerClient;

    public AppContext() {
        this.model = new SessionState();
        this.windowManager = new WindowManager(this);

        HttpClient httpClient = HttpClient.newHttpClient();

        ObjectMapper objectMapper = new ObjectMapper();

        this.authClient = new AuthClient(SessionState.BASE_URL, httpClient, objectMapper);
        this.adminClient = new AdminClient(SessionState.BASE_URL, httpClient, objectMapper);
        this.sellerClient = new SellerClient(SessionState.BASE_URL, httpClient, objectMapper);
        this.managerClient = new ManagerClient(SessionState.BASE_URL, httpClient, objectMapper);
    }

    public SessionState getSessionState() {
        return model;
    }

    public WindowManager getWindowManager() {
        return windowManager;
    }

    public AuthClient getAuthClient() {
        return authClient;
    }

    public AdminClient getAdminClient() {
        return adminClient;
    }

    public SellerClient getSellerClient() {
        return sellerClient;
    }

    public ManagerClient getManagerClient() {
        return managerClient;
    }
}
