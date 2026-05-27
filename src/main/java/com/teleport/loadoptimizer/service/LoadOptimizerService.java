package com.teleport.loadoptimizer.service;

import com.teleport.loadoptimizer.exception.InvalidRequestException;
import com.teleport.loadoptimizer.model.request.OptimizationPreferences;
import com.teleport.loadoptimizer.model.request.OptimizeRequest;
import com.teleport.loadoptimizer.model.request.OrderInput;
import com.teleport.loadoptimizer.model.response.OptimizeResponse;
import com.teleport.loadoptimizer.model.response.ParetoSolution;
import com.teleport.loadoptimizer.optimizer.SolverResult;
import com.teleport.loadoptimizer.validator.OptimizationRequestValidator;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class LoadOptimizerService {

    private final OptimizationRequestValidator validator;
    private final OptimizationCache optimizationCache;

    public LoadOptimizerService(
            OptimizationRequestValidator validator,
            OptimizationCache optimizationCache) {
        this.validator = validator;
        this.optimizationCache = optimizationCache;
    }

    public OptimizeResponse optimize(OptimizeRequest request) {
        validator.validate(request);
        CacheRequest cacheRequest = buildNormalisedCacheRequest(request);
        List<SolverResult> paretoFrontier = optimizationCache.computeParetoFrontier(cacheRequest);

        if (paretoFrontier.isEmpty()) {
            throw new InvalidRequestException(
                    "No combination of orders fits within the truck's weight and volume limits");
        }

        OptimizationPreferences prefs = request.preferences() != null
                ? request.preferences()
                : OptimizationPreferences.revenueOnly();

        SolverResult best = selectBest(paretoFrontier, prefs, request);
        return buildResponse(request, best, paretoFrontier);
    }

    private CacheRequest buildNormalisedCacheRequest(OptimizeRequest request) {
        List<OrderInput> sortedOrders = request.orders().stream()
                .sorted(Comparator.comparing(OrderInput::id))
                .toList();
        return new CacheRequest(request.truck(), sortedOrders);
    }

    private SolverResult selectBest(
            List<SolverResult> frontier,
            OptimizationPreferences prefs,
            OptimizeRequest request) {

        if (prefs.isRevenueOnly()) {
            return frontier.get(0);
        }

        long maxPayout = frontier.stream()
                .mapToLong(SolverResult::totalPayoutCents)
                .max()
                .orElse(1L);

        int maxWeight = request.truck().maxWeightLbs();
        int maxVolume = request.truck().maxVolumeCuft();

        return frontier.stream()
                .max(Comparator.comparingDouble(s ->
                        computeWeightedScore(s, prefs, maxPayout, maxWeight, maxVolume)))
                .orElseThrow(() -> new InvalidRequestException(
                        "Unable to select best solution – Pareto frontier is empty"));
    }

    private double computeWeightedScore(
            SolverResult s,
            OptimizationPreferences prefs,
            long maxPayout,
            int maxWeight,
            int maxVolume) {

        double normRevenue = maxPayout > 0
                ? (double) s.totalPayoutCents() / maxPayout : 0.0;
        double weightUtil = maxWeight > 0
                ? (double) s.totalWeightLbs() / maxWeight : 0.0;
        double volumeUtil = maxVolume > 0
                ? (double) s.totalVolumeCuft() / maxVolume : 0.0;
        double avgUtil = (weightUtil + volumeUtil) / 2.0;

        return prefs.revenueWeight() * normRevenue
                + prefs.utilizationWeight() * avgUtil;
    }

    private OptimizeResponse buildResponse(
            OptimizeRequest request,
            SolverResult best,
            List<SolverResult> paretoFrontier) {

        int maxWeight = request.truck().maxWeightLbs();
        int maxVolume = request.truck().maxVolumeCuft();

        List<String> selectedIds = best.chosenOrders().stream()
                .map(OrderInput::id)
                .toList();

        double weightUtil = roundToTwoDecimals(
                (double) best.totalWeightLbs() / maxWeight * 100);
        double volumeUtil = roundToTwoDecimals(
                (double) best.totalVolumeCuft() / maxVolume * 100);

        List<ParetoSolution> paretoSolutions = paretoFrontier.stream()
                .map(s -> toParetoSolution(s, maxWeight, maxVolume))
                .toList();

        return new OptimizeResponse(
                request.truck().id(),
                selectedIds,
                best.totalPayoutCents(),
                best.totalWeightLbs(),
                best.totalVolumeCuft(),
                weightUtil,
                volumeUtil,
                optimizationCache.algorithmName(),
                paretoSolutions
        );
    }

    private ParetoSolution toParetoSolution(
            SolverResult s, int maxWeight, int maxVolume) {
        return new ParetoSolution(
                s.chosenOrders().stream().map(OrderInput::id).toList(),
                s.totalPayoutCents(),
                s.totalWeightLbs(),
                s.totalVolumeCuft(),
                roundToTwoDecimals((double) s.totalWeightLbs() / maxWeight * 100),
                roundToTwoDecimals((double) s.totalVolumeCuft() / maxVolume * 100)
        );
    }

    private double roundToTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
