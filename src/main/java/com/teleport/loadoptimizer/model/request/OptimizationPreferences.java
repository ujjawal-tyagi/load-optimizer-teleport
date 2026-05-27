package com.teleport.loadoptimizer.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;

public record OptimizationPreferences(

        @DecimalMin(value = "0.0", message = "revenue_weight must be ≥ 0")
        @DecimalMax(value = "1.0", message = "revenue_weight must be ≤ 1")
        @JsonProperty("revenue_weight")
        double revenueWeight,

        @DecimalMin(value = "0.0", message = "utilization_weight must be ≥ 0")
        @DecimalMax(value = "1.0", message = "utilization_weight must be ≤ 1")
        @JsonProperty("utilization_weight")
        double utilizationWeight

) {

    public static OptimizationPreferences revenueOnly() {
        return new OptimizationPreferences(1.0, 0.0);
    }

    public boolean isRevenueOnly() {
        return utilizationWeight == 0.0;
    }
}
