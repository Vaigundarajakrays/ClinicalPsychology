package com.clinicalpsychology.app.zoom;

import com.clinicalpsychology.app.enums.ZoomContextType;
import com.clinicalpsychology.app.service.EmailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ZoomMeetingService {

    private final ZoomTokenService zoomTokenService;
    private final JavaMailSender mailSender;
    private final EmailService emailService;

    @Value("${mail.from}")
    private String mailFrom;


    private final ObjectMapper objectMapper = new ObjectMapper();

    public ZoomMeetingResponse createZoomMeetingAndNotify(String therapistEmail, String clientEmail, String therapistName, String clientName, Instant startTime, Instant endTime, Instant oldStart, ZoomContextType contextType, String therapistTimezone, String clientTimezone) throws Exception {
        String accessToken = zoomTokenService.getAccessToken();

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Zoom expects start time to be in ISO-8601 format: 2025-11-06T04:00:00Z. So instant.toString is crt.
        Map<String, Object> meetingDetails = new HashMap<>();
        meetingDetails.put("topic", "TherapistBooster Session");
        meetingDetails.put("type", 2); // Scheduled
        meetingDetails.put("start_time", startTime.toString());
        meetingDetails.put("duration", 60);
        meetingDetails.put("timezone", "UTC");

        Map<String, Object> settings = new HashMap<>();
        settings.put("join_before_host", false); // Don't allow joining before host
        settings.put("allow_multiple_devices", false); // Disallow joining from multiple devices
        settings.put("waiting_room", true); // Optional: enable waiting room
        settings.put("host_video", false); // Optional: host video OFF by default
        settings.put("participant_video", false); // Optional: participant video OFF
        settings.put("enforce_login", false); // Optional: allow guests
        settings.put("auto_recording", "none"); // Optional: It will not auto record

        meetingDetails.put("settings", settings);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(meetingDetails, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                "https://api.zoom.us/v2/users/me/meetings", request, String.class);

        if (response.getStatusCode() == HttpStatus.CREATED) {
            Map<String, Object> responseBody = objectMapper.readValue(response.getBody(), Map.class);
            String startUrl = (String) responseBody.get("start_url");
            String joinUrl = (String) responseBody.get("join_url");

            String therapistCalendarLink = generateGoogleCalendarLink(
                    "TherapistBooster Session",
                    "Zoom meeting you're hosting.",
                    startUrl, // <-- host link here
                    startTime,
                    endTime
            );

            String userCalendarLink = generateGoogleCalendarLink(
                    "TherapistBooster Session",
                    "Zoom meeting with your therapist.",
                    joinUrl, // <-- public attendee link
                    startTime,
                    endTime
            );

            ZoneId therapistTimeZone = ZoneId.of(therapistTimezone);
            ZoneId clientTimeZone = ZoneId.of(clientTimezone);

            // Date time for therapist
            String oldSessionDateTimeForTherapist = oldStart != null ? formattedDateTime(oldStart, therapistTimeZone) : null;
            String newSessionDateForTherapist = formattedDate(startTime, therapistTimeZone);
            String newSessionTimeForTherapist = formattedTime(startTime, therapistTimeZone);

            //Date Time for client
            String oldSessionDateTimeForClient = oldStart != null ? formattedDateTime(oldStart, clientTimeZone) : null;
            String newSessionDateForClient = formattedDate(startTime, clientTimeZone);
            String newSessionTimeForClient = formattedTime(startTime, clientTimeZone);

            // Prepare email content
            String clientSubject;
            String clientBody;
            String therapistSubject;
            String therapistBody;

            if(contextType.isReschedule() && oldStart != null){

                clientSubject = "Your TherapistBooster Session Has Been Rescheduled";
                therapistSubject = "Your TherapistBooster Session Has Been Rescheduled";

                clientBody = String.format("""
                Hi %s,

                Your previously scheduled session with %s on %s has been rescheduled.

                🆕 New Schedule:
                - 📅 Date: %s
                - 🕒 Time: %s (%s)
                - 📍 Location: Zoom

                🔗 Join Zoom Meeting:
                %s

                🗓️ Add to Calendar:
                %s

                If you have any questions or need assistance, feel free to reply to this email.

                Thank you,
                TherapistBooster Team
                """,
                        clientName,
                        therapistName,
                        oldSessionDateTimeForClient,
                        newSessionDateForClient,
                        newSessionTimeForClient,
                        clientTimezone,
                        joinUrl,
                        userCalendarLink
                );

                therapistBody = String.format("""
                Hi %s,

                The session with %s originally scheduled for %s has been rescheduled.

                🆕 New Schedule:
                - 📅 Date: %s
                - 🕒 Time: %s (%s)
                - 📍 Location: Zoom

                🔗 Start Zoom Meeting:
                %s

                🗓️ Add to Calendar:
                %s

                Please be sure to start the meeting on time.

                Best regards,
                TherapistBooster Team
                """,
                        therapistName,
                        clientName,
                        oldSessionDateTimeForTherapist,
                        newSessionDateForTherapist,
                        newSessionTimeForTherapist,
                        therapistTimezone,
                        startUrl,
                        therapistCalendarLink
                );

            } else{

                clientSubject = "Your TherapistBooster Zoom Session is Scheduled";
                therapistSubject = "Your TherapistBooster Zoom Session is Scheduled";

                clientBody = String.format("""
                Hi %s,

                Your Zoom session with %s has been scheduled.

                🗓️ Date: %s
                🕒 Time: %s (%s)
                📍 Location: Zoom

                🔗 Join Zoom Meeting:
                %s

                🗓️ Add to Calendar:
                %s

                See you soon!

                TherapistBooster Team
                """,
                        clientName,
                        therapistName,
                        newSessionDateForClient,
                        newSessionTimeForClient,
                        clientTimezone,
                        joinUrl,
                        userCalendarLink
                );

                therapistBody = String.format("""
                Hi %s,

                You have a new session with %s scheduled.

                🗓️ Date: %s
                🕒 Time: %s (%s)
                📍 Location: Zoom

                🔗 Start Zoom Meeting:
                %s

                🗓️ Add to Calendar:
                %s

                Best,
                TherapistBooster Team
                """,
                        therapistName,
                        clientName,
                        newSessionDateForTherapist,
                        newSessionTimeForTherapist,
                        therapistTimezone,
                        startUrl,
                        therapistCalendarLink
                );
            }

            emailService.sendEmail(therapistEmail, therapistSubject, therapistBody);
            emailService.sendEmail(clientEmail, clientSubject, clientBody);

            return ZoomMeetingResponse.builder()
                    .startUrl(startUrl)
                    .joinUrl(joinUrl)
                    .build();
        } else {
            throw new Exception("Failed to create Zoom meeting.");
        }
    }

    private String generateGoogleCalendarLink(String title, String details, String joinUrl, Instant startTime, Instant endTime) {

        // 🔬 Why Instant.toString() fails here?
        // Because it returns: 2025-11-06T04:00:00Z
        // But Google wants: 20251106T040000Z
        String startStr = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'")
                .withZone(ZoneOffset.UTC)
                .format(startTime);

        String endStr = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'")
                .withZone(ZoneOffset.UTC)
                .format(endTime);


        return String.format(
                "https://calendar.google.com/calendar/r/eventedit?text=%s&details=%s%%0AJoin+Here:%%20%s&location=Zoom&dates=%s/%s",
                encode(title),
                encode(details),
                encode(joinUrl),
                startStr,
                endStr
        );
    }




    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }



    private String formattedDateTime(Instant instant, ZoneId timezone) {
        return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(timezone).format(instant);
    }



    private String formattedDate(Instant instant, ZoneId timezone){
        return DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(timezone).format(instant);
    }



    private String formattedTime(Instant instant, ZoneId timezone){
        return DateTimeFormatter.ofPattern("hh:mm a").withZone(timezone).format(instant);
    }

}
