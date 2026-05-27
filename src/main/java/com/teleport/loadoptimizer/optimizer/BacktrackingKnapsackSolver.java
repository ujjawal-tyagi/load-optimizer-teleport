package com.teleport.loadoptimizer.optimizer;

import com.teleport.loadoptimizer.model.request.OrderInput;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Primary
@Component("backtrackingKnapsackSolver")
public class BacktrackingKnapsackSolver implements KnapsackSolver {

    @Override
    public Optional<SolverResult> solve(
            List<OrderInput> candidates, int maxWeightLbs, int maxVolumeCuft) {

        if (candidates == null || candidates.isEmpty()) {
            return Optional.empty();
        }

        List<OrderInput> sorted = sortByPayoutDensity(candidates);
        SearchState state = new SearchState(maxWeightLbs, maxVolumeCuft);

        backtrack(sorted, 0, 0L, 0, 0, state, new ArrayList<>());

        return state.bestPayout > 0
                ? Optional.of(new SolverResult(
                        state.bestOrders, state.bestPayout, state.bestWeight, state.bestVolume))
                : Optional.empty();
    }

    @Override
    public List<SolverResult> solvePareto(
            List<OrderInput> candidates, int maxWeightLbs, int maxVolumeCuft) {

        if (candidates == null || candidates.isEmpty()) {
            return List.of();
        }

        List<OrderInput> sorted = sortByPayoutDensity(candidates);
        List<SolverResult> feasible = new ArrayList<>();

        enumerateAllFeasible(sorted, 0, 0L, 0, 0,
                maxWeightLbs, maxVolumeCuft, feasible, new ArrayList<>());

        return ParetoUtil.computeFrontier(feasible, maxWeightLbs, maxVolumeCuft);
    }

    private void backtrack(
            List<OrderInput> sorted,
            int index,
            long currentPayout,
            int currentWeight,
            int currentVolume,
            SearchState state,
            List<OrderInput> current) {

        if (currentPayout > state.bestPayout) {
            state.bestPayout = currentPayout;
            state.bestWeight = currentWeight;
            state.bestVolume = currentVolume;
            state.bestOrders = List.copyOf(current);
        }

        if (index >= sorted.size()) {
            return;
        }

        double ub = upperBound(sorted, index, currentPayout,
                currentWeight, currentVolume, state.maxWeightLbs, state.maxVolumeCuft);
        if (ub <= state.bestPayout) {
            return;
        }

        OrderInput order = sorted.get(index);

        if (currentWeight + order.weightLbs() <= state.maxWeightLbs
                && currentVolume + order.volumeCuft() <= state.maxVolumeCuft) {
            current.add(order);
            backtrack(sorted, index + 1,
                    currentPayout + order.payoutCents(),
                    currentWeight + order.weightLbs(),
                    currentVolume + order.volumeCuft(),
                    state, current);
            current.remove(current.size() - 1);
        }

        backtrack(sorted, index + 1, currentPayout, currentWeight, currentVolume, state, current);
    }

    private void enumerateAllFeasible(
            List<OrderInput> sorted,
            int index,
            long payout,
            int weight,
            int volume,
            int maxWeight,
            int maxVolume,
            List<SolverResult> feasible,
            List<OrderInput> current) {

        if (index >= sorted.size()) {
            if (!current.isEmpty()) {
                feasible.add(new SolverResult(List.copyOf(current), payout, weight, volume));
            }
            return;
        }

        OrderInput order = sorted.get(index);

        if (weight + order.weightLbs() <= maxWeight
                && volume + order.volumeCuft() <= maxVolume) {
            current.add(order);
            enumerateAllFeasible(sorted, index + 1,
                    payout + order.payoutCents(),
                    weight + order.weightLbs(),
                    volume + order.volumeCuft(),
                    maxWeight, maxVolume, feasible, current);
            current.remove(current.size() - 1);
        }

        enumerateAllFeasible(sorted, index + 1,
                payout, weight, volume, maxWeight, maxVolume, feasible, current);
    }

    private double upperBound(
            List<OrderInput> sorted,
            int fromIndex,
            long currentPayout,
            int currentWeight,
            int currentVolume,
            int maxWeight,
            int maxVolume) {

        double bound = currentPayout;
        int remWeight = maxWeight - currentWeight;
        int remVolume = maxVolume - currentVolume;

        for (int i = fromIndex; i < sorted.size(); i++) {
            OrderInput order = sorted.get(i);

            if (order.weightLbs() <= remWeight && order.volumeCuft() <= remVolume) {
                bound += order.payoutCents();
                remWeight -= order.weightLbs();
                remVolume -= order.volumeCuft();
            } else {
                double weightFraction = remWeight > 0
                        ? (double) remWeight / order.weightLbs() : 0.0;
                double volumeFraction = remVolume > 0
                        ? (double) remVolume / order.volumeCuft() : 0.0;
                bound += Math.min(weightFraction, volumeFraction) * order.payoutCents();
                break;
            }
        }

        return bound;
    }

    private List<OrderInput> sortByPayoutDensity(List<OrderInput> candidates) {
        return candidates.stream()
                .sorted(Comparator.comparingDouble(
                        (OrderInput o) -> -(double) o.payoutCents() / Math.max(o.weightLbs(), 1)))
                .toList();
    }

    private static final class SearchState {
        final int maxWeightLbs;
        final int maxVolumeCuft;
        long bestPayout = 0;
        int bestWeight = 0;
        int bestVolume = 0;
        List<OrderInput> bestOrders = List.of();

        SearchState(int maxWeightLbs, int maxVolumeCuft) {
            this.maxWeightLbs = maxWeightLbs;
            this.maxVolumeCuft = maxVolumeCuft;
        }
    }
}
