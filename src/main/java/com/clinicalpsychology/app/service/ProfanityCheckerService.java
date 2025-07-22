package com.clinicalpsychology.app.service;

import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

@Service
public class ProfanityCheckerService {

    private final Set<String> badWords = new HashSet<>();

    @PostConstruct
    public void loadBadWords() {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new ClassPathResource("badwords.txt").getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {
                badWords.add(line.trim().toLowerCase());
            }
        } catch (Exception e) {
            System.err.println("Failed to load bad words: " + e.getMessage());
        }
    }

    public boolean containsProfanity(String input) {
        String[] words = input.toLowerCase().split("\\W+"); // Split on non-word characters
        for (String word : words) {
            if (badWords.contains(word)) {
                return true;
            }
        }
        return false;
    }

    // To convert "You're a dumb idiot" to "You're a **** *****"
    // In future may need
    public String cleanText(String input) {
        String[] words = input.split("\\b"); // Split by word boundaries
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (badWords.contains(word.toLowerCase())) {
                result.append("*".repeat(word.length()));
            } else {
                result.append(word);
            }
        }
        return result.toString();
    }
}
