package com.aihealth.healthchecker.Controller;

import com.stripe.Stripe;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Value("${stripe.secret.key}")
    private String stripeKey;

    @Value("${FRONTEND_URL:https://ai-health-checker-frontend.vercel.app}")
    private String defaultFrontendUrl;

    @PostMapping("/create-session")
    public String createSession(@RequestHeader(value = "Origin", required = false) String origin) throws Exception {

        Stripe.apiKey = stripeKey;

        String baseUrl = (origin != null && !origin.trim().isEmpty() && !origin.equals("null"))
                ? origin.trim()
                : defaultFrontendUrl;

        // Remove trailing slash if present
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }

        SessionCreateParams.LineItem.PriceData.ProductData product =
                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                        .setName("Doctor Appointment")
                        .build();

        SessionCreateParams.LineItem.PriceData priceData =
                SessionCreateParams.LineItem.PriceData.builder()
                        .setCurrency("inr")
                        .setUnitAmount(50000L) // ₹500
                        .setProductData(product)
                        .build();

        SessionCreateParams.LineItem item =
                SessionCreateParams.LineItem.builder()
                        .setQuantity(1L)
                        .setPriceData(priceData)
                        .build();

        SessionCreateParams params =
                SessionCreateParams.builder()
                        .setMode(SessionCreateParams.Mode.PAYMENT)
                        .setSuccessUrl(baseUrl + "/payment-success")
                        .setCancelUrl(baseUrl + "/payment-cancel")
                        .addLineItem(item)
                        .build();

        Session session = Session.create(params);

        return session.getUrl();
    }
}
