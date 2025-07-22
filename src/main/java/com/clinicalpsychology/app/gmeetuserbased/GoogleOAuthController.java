package com.clinicalpsychology.app.gmeetuserbased;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.Calendar;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
@RequestMapping("/oauth2")
public class GoogleOAuthController {

    @Autowired
    private GoogleOAuthService googleOAuthService;

    @Autowired
    private GoogleCalendarServic googleCalendarService;

    // Step 1: Redirect user to Google Login
    @GetMapping("/authorize")
    public void authorize(HttpServletResponse response) throws Exception {
        String url = googleOAuthService.getAuthorizationUrl();
        response.sendRedirect(url);
    }

    // Step 2: Handle callback
    @GetMapping("/callback/google")
    public String oauthCallback(@RequestParam("code") String code) {
        try {
            Credential credential = googleOAuthService.exchangeCodeForTokens(code);

            Calendar calendar = new Calendar.Builder(
                    com.google.api.client.googleapis.javanet.GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance(),
                    credential
            ).setApplicationName("TherapistBookingApp").build();

            // Dummy emails and time for testing
            String therapistEmail = "jvrchingam@gmail.com";
            String userEmail = "jvraja2001@gmail.com";
            Date start = new Date(System.currentTimeMillis() + 3600000); // +1 hr
            Date end = new Date(System.currentTimeMillis() + 7200000); // +2 hr

            String meetLink = googleCalendarService.createMeetEvent(calendar, therapistEmail, userEmail, start, end);

            return "Meet created: " + meetLink;

        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }
}

