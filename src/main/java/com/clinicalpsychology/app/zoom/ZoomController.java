package com.clinicalpsychology.app.zoom;//package com.clinicalpsychology.app.zoom;
//
//
//import com.clinicalpsychology.app.util.CommonFiles;
//import io.jsonwebtoken.io.IOException;
//import jakarta.servlet.http.HttpServletResponse;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.*;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.client.RestTemplate;
//
//import java.util.HashMap;
//import java.util.Map;
//
//@RestController
//@RequestMapping("/zoom")
//public class ZoomController {
//
//    @Value("${zoom.client.id}")
//    private String clientId;
//
//    @Value("${zoom.redirect.uri}")
//    private String redirectUri;
//
//    private final CommonFiles commonFiles;
//
//    public ZoomController(CommonFiles commonFiles){this.commonFiles=commonFiles;}
//
//    @GetMapping("/authorize")
//    public void redirectToZoom(HttpServletResponse response) throws IOException, java.io.IOException {
//        String url = "https://zoom.us/oauth/authorize" +
//                "?response_type=code" +
//                "&client_id=" + clientId +
//                "&redirect_uri=" + redirectUri;
//        response.sendRedirect(url);
//    }
//
//    @PostMapping("/create-meeting")
//    public ResponseEntity<String> createMeeting(@RequestParam String accessToken) {
//        RestTemplate restTemplate = new RestTemplate();
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setBearerAuth(accessToken);
//        headers.setContentType(MediaType.APPLICATION_JSON);
//
//        Map<String, Object> meetingDetails = new HashMap<>();
//        meetingDetails.put("topic", "TherapistBooster Session");
//        meetingDetails.put("type", 1); // 1 = Instant, 2 = Scheduled
//        meetingDetails.put("duration", 30);
//        meetingDetails.put("timezone", "Asia/Kolkata");
//
//        HttpEntity<Map<String, Object>> request = new HttpEntity<>(meetingDetails, headers);
//
//        ResponseEntity<String> response = restTemplate.postForEntity(
//                "https://api.zoom.us/v2/users/me/meetings", request, String.class);
//
//        return response;
//    }
//
//
//
//}
