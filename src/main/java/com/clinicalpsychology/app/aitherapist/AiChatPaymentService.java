package com.clinicalpsychology.app.aitherapist;

import com.clinicalpsychology.app.enums.PaymentStatus;
import com.clinicalpsychology.app.exception.ResourceNotFoundException;
import com.clinicalpsychology.app.exception.UnexpectedServerException;
import com.clinicalpsychology.app.model.ClientProfile;
import com.clinicalpsychology.app.payment.PaymentRequest;
import com.clinicalpsychology.app.payment.PaymentResponse;
import com.clinicalpsychology.app.repository.ClientProfileRepository;
import com.clinicalpsychology.app.response.CommonResponse;
import com.clinicalpsychology.app.service.EmailService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.Refund;
import com.stripe.model.checkout.Session;
import com.stripe.param.RefundCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.clinicalpsychology.app.util.Constant.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiChatPaymentService {

    @Value("${stripe.apikey}")
    private String stripeApiKey;

    @Value("${stripe.successUrl}")
    private String successUrl;

    @Value("${stripe.cancelUrl}")
    private String cancelUrl;

    private final ClientProfileRepository clientProfileRepository;
    private final AiChatPaymentRepository aiChatPaymentRepository;
    private final ObjectMapper objectMapper;
    private final EmailService emailService;

    // set the api key globally, if used refund method in a separate file and mark it as @Component no need to once again set apiKey in that file.
    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeApiKey;
    }

    public CommonResponse<PaymentResponse> checkoutProducts(AiChatPaymentDto aiChatPaymentDto) throws ResourceNotFoundException, UnexpectedServerException, StripeException {

        ClientProfile clientProfile = clientProfileRepository.findById(aiChatPaymentDto.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + aiChatPaymentDto.getClientId()));

        try {

            AiChatPayment savedPayment = AiChatPayment.builder()
                    .clientId(aiChatPaymentDto.getClientId())
                    .amount(aiChatPaymentDto.getAmount())
                    .quantity(aiChatPaymentDto.getQuantity())
                    .productName(aiChatPaymentDto.getProductName())
                    .currency(aiChatPaymentDto.getCurrency())
                    .build();

            //Stripe session creation
            Session session = null;
            SessionCreateParams.LineItem.PriceData.ProductData productData
                    = SessionCreateParams.LineItem.PriceData.ProductData.builder()
                    .setName(aiChatPaymentDto.getProductName()).build();

            SessionCreateParams.LineItem.PriceData priceData = SessionCreateParams.LineItem.PriceData.builder()
                    .setCurrency(aiChatPaymentDto.getCurrency() == null ? "CAD" : aiChatPaymentDto.getCurrency())
                    .setUnitAmount(Math.round(aiChatPaymentDto.getAmount() * 100)) // To paise or cents
                    .setProductData(productData)
                    .build();

            SessionCreateParams.LineItem lineItem = SessionCreateParams.LineItem.builder()
                    .setQuantity(aiChatPaymentDto.getQuantity())
                    .setPriceData(priceData)
                    .build();

            // If we not give payment method type, by default it takes card,
            // If Wants to support UPI, AfterPay, or others, we need to specify in the method
            SessionCreateParams.Builder builder = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(successUrl + "/{CHECKOUT_SESSION_ID}")
                    .setCancelUrl(cancelUrl + "/{CHECKOUT_SESSION_ID}")
                    .addLineItem(lineItem)
                    .putMetadata("clientEmail", clientProfile.getEmail())
                    .putMetadata("purchaseType", "ai-chat");
            //.addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)

            SessionCreateParams params = builder.build();

            session = Session.create(params);

            savedPayment.setStripeSessionId(session.getId());
            aiChatPaymentRepository.save(savedPayment);


            PaymentResponse paymentResponse = PaymentResponse.builder()
                    .status(SUCCESS)
                    .message(PAYMENT_SESSION_CREATED)
                    .sessionId(session.getId())
                    .sessionUrl(session.getUrl())
                    .build();

            return CommonResponse.<PaymentResponse>builder()
                    .message(LOADED_SESSION_URL)
                    .status(STATUS_TRUE)
                    .data(paymentResponse)
                    .statusCode(SUCCESS_CODE)
                    .build();

        } catch (StripeException e) {
            throw e;
        } catch (Exception e) {
            throw new UnexpectedServerException(ERROR_CREATING_PAYMENT_SESSION + e.getMessage());
        }

    }

    // Don't remove this method
    public String refundBooking(String paymentIntentId) throws StripeException, ResourceNotFoundException {


        RefundCreateParams params = RefundCreateParams.builder()
                .setPaymentIntent(paymentIntentId)
                .setReason(RefundCreateParams.Reason.REQUESTED_BY_CUSTOMER)
                .build();

        Refund refund = Refund.create(params);

        return refund.getId();
    }

    @Async
    public void handleSessionEventAsync(Event event, PaymentStatus paymentStatus) {

        String stripeSessionId = null;
        String paymentIntentId = null;

        try {
            JsonNode data = objectMapper.readTree(event.getData().getObject().toJson());
            stripeSessionId = data.get("id").asText();
            paymentIntentId = data.has("payment_intent") && !data.get("payment_intent").isNull() ? data.get("payment_intent").asText() : null;
        } catch (Exception e) {
            log.info("Unable to get clientEmail from webhook payload");
            return;
        }

        AiChatPayment aiChatPayment = aiChatPaymentRepository.findByStripeSessionId(stripeSessionId);

        if (aiChatPayment == null) {
            log.warn("No AiChatPayment found for sessionId: {}", stripeSessionId);
            return;
        }

        Optional<ClientProfile> clientOpt = clientProfileRepository.findById(aiChatPayment.getClientId());
        if(clientOpt.isEmpty()){
            log.warn("Client not found with id: {}", aiChatPayment.getClientId());
            return;
        }

        ClientProfile clientProfile = clientOpt.get();

        aiChatPayment.setPaymentStatus(paymentStatus);


        if(paymentStatus == PaymentStatus.COMPLETED){
            aiChatPayment.setStripePaymentIntentId(paymentIntentId);
            clientProfile.setPaidForAiChat(true);

            emailService.sendAiChatPaymentSuccessEmail(clientProfile, aiChatPayment);

        }

        aiChatPaymentRepository.save(aiChatPayment);
        clientProfileRepository.save(clientProfile);
        log.info("🔄 Successfully processed the payment for the client: {} with payment status {}", clientProfile.getEmail(), aiChatPayment.getPaymentStatus());

    }
}