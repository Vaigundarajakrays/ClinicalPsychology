package com.clinicalpsychology.app.controller;

import com.clinicalpsychology.app.dto.BookingDTO;
import com.clinicalpsychology.app.exception.ResourceNotFoundException;
import com.clinicalpsychology.app.exception.UnexpectedServerException;
import com.clinicalpsychology.app.payment.PaymentResponse;
import com.clinicalpsychology.app.payment.PaymentService;
import com.clinicalpsychology.app.response.CommonResponse;
import com.stripe.exception.StripeException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PreAuthorize("hasAnyAuthority('ROLE_CLIENT', 'ROLE_THERAPIST')")
    @PostMapping("/checkout")
    public CommonResponse<PaymentResponse> checkoutProducts(@RequestBody BookingDTO bookingDTO) throws StripeException, UnexpectedServerException, ResourceNotFoundException {
        return paymentService.checkoutProducts(bookingDTO);
    }
}
