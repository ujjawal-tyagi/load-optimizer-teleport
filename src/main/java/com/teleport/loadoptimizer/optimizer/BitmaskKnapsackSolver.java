package com.teleport.loadoptimizer.optimizer;

import com.teleport.loadoptimizer.model.request.OrderInput;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component("bitmaskKnapsackSolver")
public class BitmaskKnapsackSolver implements KnapsackSolver {

    @Override
    public Optional<SolverResult> solve(
            List<OrderInput> candidates, int maxWeightLbs, int maxVolumeCuft) {

        if (candidates == null || candidates.isEmpty()) {
            return Optional.empty();
        }

        int n = candidates.size();
        int totalMasks = 1 << n;   

        long bestPayout = 0;
        int bestMask = 0;
        int bestWeight = 0;
        int bestVolume = 0;

        for (int mask = 1; mask < totalMasks; mask++) {
            long payout = 0;
            int weight = 0;
            int volume = 0;

            for (int i = 0; i < n; i++) {
                if ((mask & (1 << i)) != 0) {
                    OrderInput order = candidates.get(i);
                    payout += order.payoutCents();
                    weight += order.weightLbs();
                    volume += order.volumeCuft();
                }
            }

            if (weight <= maxWeightLbs && volume <= maxVolumeCuft && payout > bestPayout) {
                bestPayout = payout;
                bestMask = mask;
                bestWeight = weight;
                bestVolume = volume;
            }
        }

        if (bestMask == 0) {
            return Optional.empty();
        }

        return Optional.of(new SolverResult(
                extractOrders(candidates, bestMask, n), bestPayout, bestWeight, bestVolume));
    }

    @Override
    public List<SolverResult> solvePareto(
            List<OrderInput> candidates, int maxWeightLbs, int maxVolumeCuft) {

        if (candidates == null || candidates.isEmpty()) {
            return List.of();
        }

        int n = candidates.size();
        int totalMasks = 1 << n;
        List<SolverResult> feasible = new ArrayList<>();

        for (int mask = 1; mask < totalMasks; mask++) {
            long payout = 0;
            int weight = 0;
            int volume = 0;

            for (int i = 0; i < n; i++) {
                if ((mask & (1 << i)) != 0) {
                    OrderInput order = candidates.get(i);
                    payout += order.payoutCents();
                    weight += order.weightLbs();
                    volume += order.volumeCuft();
                }
            }

            if (weight <= maxWeightLbs && volume <= maxVolumeCuft) {
                feasible.add(new SolverResult(
                        extractOrders(candidates, mask, n), payout, weight, volume));
            }
        }

        return ParetoUtil.computeFrontier(feasible, maxWeightLbs, maxVolumeCuft);
    }

    private List<OrderInput> extractOrders(List<OrderInput> candidates, int mask, int n) {
        List<OrderInput> selected = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            if ((mask & (1 << i)) != 0) {
                selected.add(candidates.get(i));
            }
        }
        return selected;
    }
}
