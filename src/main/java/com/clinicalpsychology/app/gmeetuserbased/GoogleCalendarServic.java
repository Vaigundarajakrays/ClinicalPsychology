package com.clinicalpsychology.app.gmeetuserbased;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.*;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Date;

@Service
public class GoogleCalendarServic {

    public String createMeetEvent(Calendar service, String therapistEmail, String userEmail, Date startTime, Date endTime) throws Exception {
        Event event = new Event()
                .setSummary("Therapistship Session")
                .setDescription("Therapist-User Booking with Google Meet");

        EventDateTime start = new EventDateTime()
                .setDateTime(new DateTime(startTime))
                .setTimeZone("Asia/Kolkata");
        EventDateTime end = new EventDateTime()
                .setDateTime(new DateTime(endTime))
                .setTimeZone("Asia/Kolkata");

        event.setStart(start);
        event.setEnd(end);

        event.setAttendees(Arrays.asList(
                new EventAttendee().setEmail(therapistEmail),
                new EventAttendee().setEmail(userEmail)
        ));

        ConferenceData conferenceData = new ConferenceData();
        CreateConferenceRequest conferenceRequest = new CreateConferenceRequest()
                .setRequestId("meet-" + System.currentTimeMillis());
        conferenceData.setCreateRequest(conferenceRequest);
        event.setConferenceData(conferenceData);

        Event createdEvent = service.events().insert("primary", event)
                .setConferenceDataVersion(1)
                .execute();

        return createdEvent.getHangoutLink(); // Google Meet link
    }
}
