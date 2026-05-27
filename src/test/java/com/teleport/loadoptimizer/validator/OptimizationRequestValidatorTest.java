package com.teleport.loadoptimizer.validator;

import com.teleport.loadoptimizer.exception.InvalidRequestException;
import com.teleport.loadoptimizer.exception.PayloadTooLargeException;
import com.teleport.loadoptimizer.model.request.OptimizeRequest;
import com.teleport.loadoptimizer.model.request.OrderInput;
import com.teleport.loadoptimizer.model.request.TruckInput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OptimizationRequestValidatorTest {

    private OptimizationRequestValidator validator;

    private static final TruckInput TRUCK =
            new TruckInput("truck-1", 44_000, 3_000);

    @BeforeEach
    void setUp() {
        validator = new OptimizationRequestValidator();
    }

    @Test
    void validate_shouldPassForValidRequest() {
        OptimizeRequest request = new OptimizeRequest(TRUCK, List.of(order("ord-1")), null);
        assertThatNoException().isThrownBy(() -> validator.validate(request));
    }

    @Test
    void validate_shouldThrow413WhenMoreThan22Orders() {
        List<OrderInput> orders = new ArrayList<>();
        for (int i = 1; i <= 23; i++) {
            orders.add(order("ord-" + i));
        }
        OptimizeRequest request = new OptimizeRequest(TRUCK, orders, null);

        assertThatThrownBy(() -> validator.validate(request))
                .isInstanceOf(PayloadTooLargeException.class)
                .hasMessageContaining("23");
    }

    @Test
    void validate_shouldThrowOnDuplicateOrderIds() {
        OptimizeRequest request = new OptimizeRequest(
                TRUCK, List.of(order("dup"), order("dup")), null);

        assertThatThrownBy(() -> validator.validate(request))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("dup");
    }

    @Test
    void validate_shouldThrowWhenPickupAfterDelivery() {
        OrderInput bad = new OrderInput(
                "bad", 100_000L, 1_000, 100, "LA", "Dallas",
                LocalDate.of(2025, 12, 10),   
                LocalDate.of(2025, 12, 5),
                false);

        OptimizeRequest request = new OptimizeRequest(TRUCK, List.of(bad), null);

        assertThatThrownBy(() -> validator.validate(request))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("bad");
    }

    @Test
    void validate_shouldThrowWhenSingleOrderExceedsTruckWeight() {
        TruckInput smallTruck = new TruckInput("tiny", 1_000, 3_000);
        OrderInput heavy = new OrderInput(
                "heavy", 100_000L, 5_000, 100, "LA", "Dallas",
                LocalDate.of(2025, 12, 5), LocalDate.of(2025, 12, 9), false);

        OptimizeRequest request = new OptimizeRequest(smallTruck, List.of(heavy), null);

        assertThatThrownBy(() -> validator.validate(request))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("heavy")
                .hasMessageContaining("weight");
    }

    @Test
    void validate_shouldThrowWhenSingleOrderExceedsTruckVolume() {
        TruckInput smallTruck = new TruckInput("tiny", 44_000, 100);
        OrderInput bulky = new OrderInput(
                "bulky", 100_000L, 1_000, 5_000, "LA", "Dallas",
                LocalDate.of(2025, 12, 5), LocalDate.of(2025, 12, 9), false);

        OptimizeRequest request = new OptimizeRequest(smallTruck, List.of(bulky), null);

        assertThatThrownBy(() -> validator.validate(request))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("bulky")
                .hasMessageContaining("volume");
    }

    @Test
    void validate_shouldPassWhenExactly22Orders() {
        List<OrderInput> orders = new ArrayList<>();
        for (int i = 1; i <= 22; i++) {
            orders.add(order("ord-" + i));
        }
        OptimizeRequest request = new OptimizeRequest(TRUCK, orders, null);
        assertThatNoException().isThrownBy(() -> validator.validate(request));
    }

    private OrderInput order(String id) {
        return new OrderInput(
                id, 100_000L, 1_000, 100, "LA", "Dallas",
                LocalDate.of(2025, 12, 5), LocalDate.of(2025, 12, 9), false);
    }
}
