package com.clinicalpsychology.app.exceptionHandling;

import com.stripe.exception.StripeException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static com.clinicalpsychology.app.util.Constant.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> handleResourceNotFound(ResourceNotFoundException ex) {
        ErrorResponse errorResponse=
                new ErrorResponse(ex.getMessage()
                        ,NO_DATA,STATUS_FALSE, LocalDateTime.now());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UnexpectedServerException.class)
    public ResponseEntity<?> handleUnexpectedServerError(UnexpectedServerException ex){

        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage(), SERVER_ERROR, STATUS_FALSE, LocalDateTime.now());

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(InvalidFieldValueException.class)
    public ResponseEntity<ErrorResponse> handleInvalidFieldValue(InvalidFieldValueException ex) {
        ErrorResponse error = new ErrorResponse(
                ex.getMessage(),
                "INVALID",
                false,
                LocalDateTime.now()
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleResourceAlreadyExists(ResourceAlreadyExistsException ex) {
        ErrorResponse error = new ErrorResponse(
                ex.getMessage(),
                "RESOURCE_ALREADY_EXISTS",
                false,
                LocalDateTime.now()
        );
        return new ResponseEntity<>(error, HttpStatus.CONFLICT); // 409 Conflict
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(OtpException.class)
    public ResponseEntity<ErrorResponse> handleOtpException(OtpException ex) {
        ErrorResponse error = new ErrorResponse(
                ex.getMessage(),
                ex.getErrorCode(),
                false,
                LocalDateTime.now()
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // This exception is thrown before your controller method is even called.
    // For example, if the request JSON has an invalid date format like:
    // "joiningDate": "2025-09-2025" but your entity expects a LocalDate,
    // Jackson fails to deserialize it and throws a DateTimeParseException.
    // Since the controller method is never reached, custom exceptions like
    // UnexpectedServerException won't be triggered.
    // To handle this, define a global exception handler for HttpMessageNotReadableException.
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleInvalidJson(HttpMessageNotReadableException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                "Invalid input format: " + ex.getMostSpecificCause().getMessage(),
                SERVER_ERROR,
                STATUS_FALSE,
                LocalDateTime.now()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // Spring's multipart resolver (the thing that processes file uploads) is configured as a bean in the application context.
    //And when it throws MaxUploadSizeExceededException, it’s still inside the Spring MVC pipeline,
    // which allows @ControllerAdvice's @ExceptionHandler to catch it — as long as the exception bubbles up and isn't swallowed.
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxSizeException(MaxUploadSizeExceededException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                "File size too large! " + ex.getMessage(),
                SERVER_ERROR,
                STATUS_FALSE,
                LocalDateTime.now()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.PAYLOAD_TOO_LARGE);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleUnsupportedMediaType(HttpMediaTypeNotSupportedException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                "Unsupported Media Type: " + ex.getMessage(),
                SERVER_ERROR,
                STATUS_FALSE,
                LocalDateTime.now()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }



    //Exception Handling for JWT token
    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<?> handleExpiredJwtException(ExpiredJwtException ex) {
        ErrorResponse errorResponse=
                new ErrorResponse(SESSION_EXPIRED
                        ,TOKEN_EXPIRED,STATUS_FALSE, LocalDateTime.now());
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }
    @ExceptionHandler(SignatureException.class)
    public ResponseEntity<?> handleInvalidJwtSignature(SignatureException ex) {
        ErrorResponse errorResponse=
                new ErrorResponse(JWT_SIGNATURE_MISMATCH
                        ,INVALID_JWT_SIGNATURE,STATUS_FALSE, LocalDateTime.now());
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }
    @ExceptionHandler(MalformedJwtException.class)
    public ResponseEntity<?> handleMalformedJwtException(MalformedJwtException ex) {
        ErrorResponse errorResponse=
                new ErrorResponse(JWT_FORMAT_INCORRECT
                        ,MALFORMED_JWT_TOKEN,STATUS_FALSE, LocalDateTime.now());
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(UnsupportedJwtException.class)
    public ResponseEntity<?> handleUnsupportedJwtException(UnsupportedJwtException ex) {
        ErrorResponse errorResponse=
                new ErrorResponse(JWT_NOT_SUPPORTED
                        ,UNSUPPORTED_JWT_TOKEN,STATUS_FALSE, LocalDateTime.now());
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    // All exception
//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<?> handleGeneralException(Exception ex) {
//        ErrorResponse errorResponse=
//                new ErrorResponse(ex.getMessage()
//                        ,ERROR,STATUS_FALSE, LocalDateTime.now());
//        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
//    }

    // Exception Handling for login (Authentication)
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<?> handleBadCredentialException(BadCredentialsException ex) {
        ErrorResponse errorResponse=
                new ErrorResponse(INVALID_USERNAME_PASSWORD
                        ,ex.getMessage(),STATUS_FALSE, LocalDateTime.now());
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    // Exception Handling for SMTP
    @ExceptionHandler(MailAuthenticationException.class)
    public ResponseEntity<?> handleMailAuthenticationException(MailAuthenticationException ex) {
        ErrorResponse errorResponse=
                new ErrorResponse(EMAIL_AUTH_FAILED_SENDER
                        ,ex.getMessage(),STATUS_FALSE, LocalDateTime.now());
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }
    @ExceptionHandler(MailSendException.class)
    public ResponseEntity<?> handleMailSendException(MailSendException ex) {
        ErrorResponse errorResponse=
                new ErrorResponse(EMAIL_AUTH_FAILED_RECIPIENT
                        ,ex.getMessage(),STATUS_FALSE, LocalDateTime.now());
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(MailException.class)
    public ResponseEntity<?> handleMailException(MailException ex) {
        ErrorResponse errorResponse=
                new ErrorResponse(EMAIL_SENDING_FAILED
                        ,ex.getMessage(),STATUS_FALSE, LocalDateTime.now());
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    // Exception Handling for Stripe payment
    @ExceptionHandler(StripeException.class)
    public ResponseEntity<?> handleStripeException(StripeException ex) {
        ErrorResponse errorResponse=
                new ErrorResponse(PAYMENT_ERROR
                        ,ex.getMessage(),STATUS_FALSE, LocalDateTime.now());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }






}
