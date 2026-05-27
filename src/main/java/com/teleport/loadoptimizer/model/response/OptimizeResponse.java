package com.teleport.loadoptimizer.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record OptimizeResponse(

        @JsonProperty("truck_id")
        String truckId,

        @JsonProperty("selected_order_ids")
        List<String> selectedOrderIds,

        @JsonProperty("total_payout_cents")
        long totalPayoutCents,

        @JsonProperty("total_weight_lbs")
        int totalWeightLbs,

        @JsonProperty("total_volume_cuft")
        int totalVolumeCuft,

        @JsonProperty("utilization_weight_percent")
        double utilizationWeightPercent,

        @JsonProperty("utilization_volume_percent")
        double utilizationVolumePercent,

        @JsonProperty("algorithm_used")
        String algorithmUsed,

        @JsonProperty("pareto_solutions")
        List<ParetoSolution> paretoSolutions

) {}
