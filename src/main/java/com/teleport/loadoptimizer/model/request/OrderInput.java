package com.teleport.loadoptimizer.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record OrderInput(
        @NotBlank(message = "order id must not be blank")
        String id,

        @Positive(message = "payout_cents must be a positive integer")
        @JsonProperty("payout_cents")
        long payoutCents,

        @Positive(message = "weight_lbs must be a positive number")
        @JsonProperty("weight_lbs")
        int weightLbs,

        @Positive(message = "volume_cuft must be a positive number")
        @JsonProperty("volume_cuft")
        int volumeCuft,

        @NotBlank(message = "origin must not be blank")
        String origin,

        @NotBlank(message = "destination must not be blank")
        String destination,

        @NotNull(message = "pickup_date must not be null")
        @JsonProperty("pickup_date")
        LocalDate pickupDate,

        @NotNull(message = "delivery_date must not be null")
        @JsonProperty("delivery_date")
        LocalDate deliveryDate,

        @JsonProperty("is_hazmat")
        boolean isHazmat
) {}
