package com.clinicalpsychology.app.aitherapist;

import com.clinicalpsychology.app.exception.UnexpectedServerException;
import com.clinicalpsychology.app.response.CommonResponse;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.clinicalpsychology.app.util.Constant.*;

@Service
public class TempTokenService {

    private final Map<String, TempToken> tokenStore = new ConcurrentHashMap<>();

    private static final long TOKEN_EXPIRY_MINUTES = 20;

    public CommonResponse<String> generateToken(String username) throws UnexpectedServerException {

        try {

            // First, check if any existing token for this username is present & valid
            Optional<Map.Entry<String, TempToken>> existingEntry = tokenStore.entrySet().stream()
                    .filter(entry -> entry.getValue().username().equals(username))
                    .filter(entry -> entry.getValue().expiry().isAfter(Instant.now()))
                    .findFirst();

            if (existingEntry.isPresent()) {
                // If found and not expired, return existing token
                String token = existingEntry.get().getKey();
                return CommonResponse.<String>builder()
                        .status(STATUS_TRUE)
                        .statusCode(SUCCESS_CODE)
                        .message(TOKEN_SUCCESSFULLY_GENERATED)
                        .data(token)
                        .build();

            }

            // Otherwise, remove all expired/old tokens for this user (optional cleanup)
            tokenStore.entrySet().removeIf(entry ->
                    entry.getValue().username().equals(username)
            );

            // Generate and store a new token
            String token = UUID.randomUUID().toString();
            Instant expiry = Instant.now().plus(Duration.ofMinutes(TOKEN_EXPIRY_MINUTES));
            tokenStore.put(token, new TempToken(username, expiry));

            return CommonResponse.<String>builder()
                    .status(STATUS_TRUE)
                    .statusCode(SUCCESS_CODE)
                    .message(TOKEN_SUCCESSFULLY_GENERATED)
                    .data(token)
                    .build();


//        System.out.println("Current Token Store:");
//        tokenStore.forEach((key, value) -> {
//            System.out.println("Token: " + key);
//            System.out.println("Username: " + value.username());
//            System.out.println("Expires At: " + value.expiry());
//            System.out.println("----------");
//        });

        } catch (Exception e){
            throw new UnexpectedServerException(ERROR_GENERATING_TOKEN+ e.getMessage());
        }

    }

    public boolean isValid(String token) {
        TempToken temp = tokenStore.get(token);
        if (temp == null || temp.expiry().isBefore(Instant.now())) {
            tokenStore.remove(token); // Clean up expired token
            return false;
        }
        return true;
    }

    public String getUsername(String token) {
        return tokenStore.get(token).username();
    }


    // Optional cleanup method for expired tokens (can run as scheduled task)
    public void cleanupExpiredTokens() {
        Instant now = Instant.now();
        tokenStore.entrySet().removeIf(e -> e.getValue().expiry().isBefore(now));
    }

    // static class is used only in nested class
    // below code not generate getter like getUsername, we can just simply use key.username() to get the username
    private record TempToken(String username, Instant expiry) {}

    // the above can be written as
//    private static class TempToken {
//        private final String username;
//        private final Instant expiry;
//
//        public TempToken(String username, Instant expiry) {
//            this.username = username;
//            this.expiry = expiry;
//        }
//
//        public String getUsername() {
//            return username;
//        }
//
//        public Instant getExpiry() {
//            return expiry;
//        }
//    }
}

