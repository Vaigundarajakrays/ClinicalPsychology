package com.clinicalpsychology.app.zoom;//package com.clinicalpsychology.app.zoom;
//
//import jakarta.annotation.PostConstruct;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.HttpEntity;
//import org.springframework.http.ResponseEntity;
//import org.springframework.util.LinkedMultiValueMap;
//import org.springframework.util.MultiValueMap;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//import org.springframework.web.client.RestTemplate;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.MediaType;
//
//
//import java.util.Map;
//
//@RestController
//@RequestMapping("/zoom")
//public class ZoomCallBackController {
//
//    @Value("${zoom.client.id}")
//    private String clientId;
//
//    @Value("${zoom.client.secret}")
//    private String clientSecret;
//
//    @Value("${zoom.redirect.uri}")
//    private String redirectUri;
//
//    @PostConstruct
//    public void init() {
//        // Optionally store token on server start
//    }
//
//    @GetMapping("/oauth/callback")
//    public String handleCallback(@RequestParam("code") String code) throws Exception {
//        RestTemplate restTemplate = new RestTemplate();
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setBasicAuth(clientId, clientSecret);
//        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
//
//        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
//        body.add("grant_type", "authorization_code");
//        body.add("code", code);
//        body.add("redirect_uri", redirectUri);
//
//        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
//        ResponseEntity<Map> response = restTemplate.postForEntity("https://zoom.us/oauth/token", request, Map.class);
//
//        String accessToken = response.getBody().get("access_token").toString();
//        return "Access Token: " + accessToken;
//    }
//}
