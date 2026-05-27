package com.teleport.loadoptimizer.optimizer;

import com.teleport.loadoptimizer.model.request.OrderInput;

import java.util.List;

public record SolverResult(
        List<OrderInput> chosenOrders,
        long totalPayoutCents,
        int totalWeightLbs,
        int totalVolumeCuft) {}
