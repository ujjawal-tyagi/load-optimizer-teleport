package com.teleport.loadoptimizer.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record TruckInput(
        @NotBlank(message = "truck id must not be blank")
        String id,

        @Positive(message = "max_weight_lbs must be a positive number")
        @JsonProperty("max_weight_lbs")
        int maxWeightLbs,

        @Positive(message = "max_volume_cuft must be a positive number")
        @JsonProperty("max_volume_cuft")
        int maxVolumeCuft
) {}
