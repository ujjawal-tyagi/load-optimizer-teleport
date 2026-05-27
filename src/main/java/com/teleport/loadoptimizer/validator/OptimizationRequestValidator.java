package com.teleport.loadoptimizer.validator;

import com.teleport.loadoptimizer.exception.InvalidRequestException;
import com.teleport.loadoptimizer.exception.PayloadTooLargeException;
import com.teleport.loadoptimizer.model.request.OptimizeRequest;
import com.teleport.loadoptimizer.model.request.OrderInput;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class OptimizationRequestValidator {

    private static final int MAX_ORDERS = 22;

    public void validate(OptimizeRequest request) {
        ensureOrderCountWithinLimit(request);
        ensureNoDuplicateOrderIds(request);
        ensurePickupBeforeDelivery(request);
        ensureEachOrderFitsTruck(request);
    }

    private void ensureOrderCountWithinLimit(OptimizeRequest request) {
        if (request.orders().size() > MAX_ORDERS) {
            throw new PayloadTooLargeException(
                    String.format("Too many orders: maximum is %d, received %d. "
                                    + "Split the request into smaller batches.",
                            MAX_ORDERS, request.orders().size()));
        }
    }

    private void ensureNoDuplicateOrderIds(OptimizeRequest request) {
        Set<String> seen = new HashSet<>();
        for (OrderInput order : request.orders()) {
            if (!seen.add(order.id())) {
                throw new InvalidRequestException(
                        "Duplicate order ID: '" + order.id() + "'");
            }
        }
    }

    private void ensurePickupBeforeDelivery(OptimizeRequest request) {
        for (OrderInput order : request.orders()) {
            if (order.pickupDate().isAfter(order.deliveryDate())) {
                throw new InvalidRequestException(
                        "Order '" + order.id() + "': pickup_date ("
                                + order.pickupDate() + ") must not be after delivery_date ("
                                + order.deliveryDate() + ")");
            }
        }
    }

    private void ensureEachOrderFitsTruck(OptimizeRequest request) {
        int maxWeight = request.truck().maxWeightLbs();
        int maxVolume = request.truck().maxVolumeCuft();

        for (OrderInput order : request.orders()) {
            if (order.weightLbs() > maxWeight) {
                throw new InvalidRequestException(
                        String.format("Order '%s' weight (%d lbs) exceeds truck capacity (%d lbs)",
                                order.id(), order.weightLbs(), maxWeight));
            }
            if (order.volumeCuft() > maxVolume) {
                throw new InvalidRequestException(
                        String.format("Order '%s' volume (%d cuft) exceeds truck capacity (%d cuft)",
                                order.id(), order.volumeCuft(), maxVolume));
            }
        }
    }
}
