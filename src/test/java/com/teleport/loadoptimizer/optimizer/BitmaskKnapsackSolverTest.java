package com.teleport.loadoptimizer.optimizer;

import com.teleport.loadoptimizer.model.request.OrderInput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class BitmaskKnapsackSolverTest {

    private KnapsackSolver solver;

    @BeforeEach
    void setUp() {
        solver = new BitmaskKnapsackSolver();
    }

    @Test
    void solve_shouldSelectOptimalOrdersFromPdfExample() {
        
        OrderInput ord001 = order("ord-001", 250_000L, 18_000, 1_200);
        OrderInput ord002 = order("ord-002", 180_000L, 12_000, 900);
        OrderInput ord003 = order("ord-003", 320_000L, 30_000, 1_800);

        Optional<SolverResult> result = solver.solve(List.of(ord001, ord002, ord003), 44_000, 3_000);

        assertThat(result).isPresent();
        assertThat(result.get().totalPayoutCents()).isEqualTo(500_000L);
        assertThat(result.get().totalWeightLbs()).isEqualTo(42_000);
        assertThat(result.get().chosenOrders()).extracting(OrderInput::id)
                .containsExactlyInAnyOrder("ord-002", "ord-003");
    }

    @Test
    void solve_shouldReturnEmptyWhenNoOrderFitsTheTruck() {
        OrderInput heavy = order("heavy", 999_999L, 99_999, 99_999);
        assertThat(solver.solve(List.of(heavy), 100, 100)).isEmpty();
    }

    @Test
    void solve_shouldReturnEmptyForEmptyCandidateList() {
        assertThat(solver.solve(List.of(), 44_000, 3_000)).isEmpty();
    }

    @Test
    void solve_shouldReturnEmptyForNullCandidateList() {
        assertThat(solver.solve(null, 44_000, 3_000)).isEmpty();
    }

    @Test
    void solve_shouldSelectSingleOrderWhenOnlyOneFits() {
        OrderInput small = order("small", 100_000L, 5_000, 500);
        OrderInput huge  = order("huge",  999_999L, 50_000, 5_000);

        Optional<SolverResult> result = solver.solve(List.of(small, huge), 10_000, 1_000);

        assertThat(result).isPresent();
        assertThat(result.get().chosenOrders()).hasSize(1);
        assertThat(result.get().chosenOrders().get(0).id()).isEqualTo("small");
    }

    @Test
    void solve_shouldSelectAllOrdersWhenAllFit() {
        OrderInput a = order("a", 100_000L, 1_000, 100);
        OrderInput b = order("b", 200_000L, 2_000, 200);

        Optional<SolverResult> result = solver.solve(List.of(a, b), 44_000, 3_000);

        assertThat(result).isPresent();
        assertThat(result.get().chosenOrders()).hasSize(2);
        assertThat(result.get().totalPayoutCents()).isEqualTo(300_000L);
    }

    @Test
    void solve_shouldHandleExactCapacityFit() {
        
        OrderInput exact = order("exact", 250_000L, 44_000, 3_000);

        Optional<SolverResult> result = solver.solve(List.of(exact), 44_000, 3_000);

        assertThat(result).isPresent();
        assertThat(result.get().totalWeightLbs()).isEqualTo(44_000);
        assertThat(result.get().totalVolumeCuft()).isEqualTo(3_000);
    }

    @Test
    void solvePareto_shouldReturnNonEmptyFrontierWhenSolutionsExist() {
        OrderInput a = order("a", 300_000L, 20_000, 2_000);
        OrderInput b = order("b", 200_000L, 10_000, 1_000);

        List<SolverResult> frontier = solver.solvePareto(List.of(a, b), 44_000, 3_000);

        assertThat(frontier).isNotEmpty();
        
        assertThat(frontier.get(0).totalPayoutCents())
                .isGreaterThanOrEqualTo(frontier.get(frontier.size() - 1).totalPayoutCents());
    }

    @Test
    void solvePareto_shouldReturnEmptyForEmptyInput() {
        assertThat(solver.solvePareto(List.of(), 44_000, 3_000)).isEmpty();
    }

    @Test
    void solvePareto_shouldReturnEmptyWhenNothingFits() {
        OrderInput massive = order("m", 999_999L, 99_999, 99_999);
        assertThat(solver.solvePareto(List.of(massive), 100, 100)).isEmpty();
    }

    private OrderInput order(String id, long payoutCents, int weightLbs, int volumeCuft) {
        return new OrderInput(
                id, payoutCents, weightLbs, volumeCuft,
                "Los Angeles, CA", "Dallas, TX",
                LocalDate.of(2025, 12, 5), LocalDate.of(2025, 12, 9),
                false);
    }
}
