package com.example.stripe_backnend.controller;

import com.stripe.Stripe;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;


@RestController
public class StripeController {

    // Inyectar la clave secreta desde application.properties
    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    @PostMapping("/create-payment-intent")
    public ResponseEntity<Map<String, String>> createPaymentIntent(@RequestBody Map<String, Object> data) {
        try {
            if (data == null || !data.containsKey("amount")) {
                return ResponseEntity.badRequest().body(Map.of("error", "Amount is required"));
            }

            Stripe.apiKey = stripeSecretKey;

            // Validar que el monto sea un número válido
            long amount;
            try {
                amount = Long.parseLong(data.get("amount").toString());
            } catch (NumberFormatException e) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid amount"));
            }

            // Crear el PaymentIntent
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amount)
                    .setCurrency("eur")
                    .build();

            PaymentIntent intent = PaymentIntent.create(params);

            // Log para revisar que la respuesta contiene el clientSecret
            System.out.println("ClientSecret: " + intent.getClientSecret());

            Map<String, String> response = new HashMap<>();
            response.put("clientSecret", intent.getClientSecret());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
