package com.teleport.loadoptimizer.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ParetoSolution(

        @JsonProperty("order_ids")
        List<String> orderIds,

        @JsonProperty("total_payout_cents")
        long totalPayoutCents,

        @JsonProperty("total_weight_lbs")
        int totalWeightLbs,

        @JsonProperty("total_volume_cuft")
        int totalVolumeCuft,

        @JsonProperty("utilization_weight_percent")
        double utilizationWeightPercent,

        @JsonProperty("utilization_volume_percent")
        double utilizationVolumePercent

) {}
