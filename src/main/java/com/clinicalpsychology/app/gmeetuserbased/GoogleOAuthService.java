package com.clinicalpsychology.app.gmeetuserbased;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.CalendarScopes;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Collections;

@Service
public class GoogleOAuthService {

    private static final String CREDENTIALS_FILE_PATH = "src/main/resources/client_secret.json";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();


    private final String redirectUri = "http://localhost:8080/oauth2/callback/google";

    public GoogleAuthorizationCodeFlow getFlow() throws Exception {
        InputStream in = new FileInputStream(CREDENTIALS_FILE_PATH);
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new java.io.InputStreamReader(in));

        return new GoogleAuthorizationCodeFlow.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY,
                clientSecrets,
                Collections.singleton(CalendarScopes.CALENDAR)
        ).setAccessType("offline")
                .setApprovalPrompt("force")
                .build();
    }

    public String getAuthorizationUrl() throws Exception {
        return getFlow().newAuthorizationUrl()
                .setRedirectUri(redirectUri)
                .toString();

    }

    public Credential exchangeCodeForTokens(String code) throws Exception {
        GoogleTokenResponse tokenResponse = getFlow().newTokenRequest(code)
                .setRedirectUri(redirectUri)
                .execute();

        return getFlow().createAndStoreCredential(tokenResponse, null);
    }
}

