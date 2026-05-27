package com.teleport.loadoptimizer.util;

import com.teleport.loadoptimizer.model.request.OrderInput;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class RouteCompatibilityUtil {

    private RouteCompatibilityUtil() {}

    public static List<List<OrderInput>> groupByCompatibility(List<OrderInput> orders) {
        
        Map<String, List<OrderInput>> byLane = new LinkedHashMap<>();

        for (OrderInput order : orders) {
            String laneKey = buildLaneKey(order);
            byLane.computeIfAbsent(laneKey, k -> new ArrayList<>()).add(order);
        }

        List<List<OrderInput>> compatibleGroups = new ArrayList<>();

        for (List<OrderInput> laneOrders : byLane.values()) {
            List<OrderInput> hazmatOrders = laneOrders.stream()
                    .filter(OrderInput::isHazmat)
                    .toList();

            List<OrderInput> regularOrders = laneOrders.stream()
                    .filter(o -> !o.isHazmat())
                    .toList();

            if (!hazmatOrders.isEmpty()) {
                compatibleGroups.add(hazmatOrders);
            }
            if (!regularOrders.isEmpty()) {
                compatibleGroups.add(regularOrders);
            }
        }

        return compatibleGroups;
    }

    private static String buildLaneKey(OrderInput order) {
        return order.origin().trim().toLowerCase() + "|" + order.destination().trim().toLowerCase();
    }
}
