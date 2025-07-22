package com.clinicalpsychology.app.zoom;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class ZoomTokenService {

    @Value("${zoom.account.id}")
    private String accountId;

    @Value("${zoom.client.id}")
    private String clientId;

    @Value("${zoom.client.secret}")
    private String clientSecret;

    public String getAccessToken() {
        RestTemplate restTemplate = new RestTemplate();

        String url = "https://zoom.us/oauth/token" +
                "?grant_type=account_credentials" +
                "&account_id=" + accountId;

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(clientId, clientSecret);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<?> request = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, request, Map.class);

        return response.getBody().get("access_token").toString();
    }
}

