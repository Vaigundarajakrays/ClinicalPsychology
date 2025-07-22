package com.clinicalpsychology.app.gmeetservicebased;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.Calendar;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.util.Collections;

@Service
public class GoogleServiceAccountConfig {

    private static final String SERVICE_ACCOUNT_KEY_PATH = "src/main/resources/boosterKey.json";
    private static final String APPLICATION_NAME = "TherapistBookingApp";

    public Calendar getCalendarService() throws Exception {
        GoogleCredential credential = GoogleCredential
                .fromStream(new FileInputStream(SERVICE_ACCOUNT_KEY_PATH))
                .createScoped(Collections.singleton("https://www.googleapis.com/auth/calendar"));

        return new Calendar.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                credential
        ).setApplicationName(APPLICATION_NAME).build();
    }
}