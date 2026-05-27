package com.teleport.loadoptimizer.util;

import com.teleport.loadoptimizer.model.request.OrderInput;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RouteCompatibilityUtilTest {

    private static final LocalDate PICKUP   = LocalDate.of(2025, 12, 5);
    private static final LocalDate DELIVERY = LocalDate.of(2025, 12, 9);

    @Test
    void groupByCompatibility_shouldGroupSameLaneTogether() {
        OrderInput a = order("a", "Los Angeles, CA", "Dallas, TX", false);
        OrderInput b = order("b", "Los Angeles, CA", "Dallas, TX", false);

        List<List<OrderInput>> groups = RouteCompatibilityUtil.groupByCompatibility(List.of(a, b));

        assertThat(groups).hasSize(1);
        assertThat(groups.get(0)).hasSize(2);
    }

    @Test
    void groupByCompatibility_shouldSeparateDifferentLanes() {
        OrderInput la2dallas = order("la-dal", "Los Angeles, CA", "Dallas, TX", false);
        OrderInput la2chicago = order("la-chi", "Los Angeles, CA", "Chicago, IL", false);

        List<List<OrderInput>> groups = RouteCompatibilityUtil.groupByCompatibility(
                List.of(la2dallas, la2chicago));

        assertThat(groups).hasSize(2);
    }

    @Test
    void groupByCompatibility_shouldIsolateHazmatFromRegularOnSameLane() {
        OrderInput regular = order("regular", "LA", "Dallas", false);
        OrderInput hazmat  = order("hazmat",  "LA", "Dallas", true);

        List<List<OrderInput>> groups = RouteCompatibilityUtil.groupByCompatibility(
                List.of(regular, hazmat));

        assertThat(groups).hasSize(2);
        
        assertThat(groups).allMatch(g -> g.size() == 1);
    }

    @Test
    void groupByCompatibility_shouldAllowMultipleHazmatOnSameLane() {
        OrderInput haz1 = order("haz1", "LA", "Dallas", true);
        OrderInput haz2 = order("haz2", "LA", "Dallas", true);

        List<List<OrderInput>> groups = RouteCompatibilityUtil.groupByCompatibility(
                List.of(haz1, haz2));

        assertThat(groups).hasSize(1);
        assertThat(groups.get(0)).hasSize(2);
    }

    @Test
    void groupByCompatibility_shouldBeCaseAndTrimInsensitiveForLaneKey() {
        OrderInput a = order("a", "  Los Angeles, CA  ", "Dallas, TX", false);
        OrderInput b = order("b", "los angeles, ca", "DALLAS, TX", false);

        List<List<OrderInput>> groups = RouteCompatibilityUtil.groupByCompatibility(List.of(a, b));

        assertThat(groups).hasSize(1);
        assertThat(groups.get(0)).hasSize(2);
    }

    @Test
    void groupByCompatibility_shouldHandleMixedScenario() {
        OrderInput laRegular1 = order("la-reg-1", "LA", "Dallas", false);
        OrderInput laRegular2 = order("la-reg-2", "LA", "Dallas", false);
        OrderInput laHazmat   = order("la-haz",   "LA", "Dallas", true);
        OrderInput sfRegular  = order("sf-reg",   "San Francisco", "Denver", false);

        List<List<OrderInput>> groups = RouteCompatibilityUtil.groupByCompatibility(
                List.of(laRegular1, laRegular2, laHazmat, sfRegular));

        assertThat(groups).hasSize(3);
    }

    private OrderInput order(String id, String origin, String destination, boolean hazmat) {
        return new OrderInput(id, 100_000L, 1_000, 100,
                origin, destination, PICKUP, DELIVERY, hazmat);
    }
}
