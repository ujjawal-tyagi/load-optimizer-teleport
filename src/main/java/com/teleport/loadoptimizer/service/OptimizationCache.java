package com.teleport.loadoptimizer.service;

import com.teleport.loadoptimizer.config.CacheConfig;
import com.teleport.loadoptimizer.model.request.OrderInput;
import com.teleport.loadoptimizer.optimizer.KnapsackSolver;
import com.teleport.loadoptimizer.optimizer.ParetoUtil;
import com.teleport.loadoptimizer.optimizer.SolverResult;
import com.teleport.loadoptimizer.util.RouteCompatibilityUtil;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class OptimizationCache {

    private static final String ALGORITHM_NAME = "Recursive Backtracking with Pruning (Branch and Bound)";

    private final KnapsackSolver solver;

    public OptimizationCache(KnapsackSolver solver) {
        this.solver = solver;
    }

    @Cacheable(value = CacheConfig.OPTIMIZATION_CACHE)
    public List<SolverResult> computeParetoFrontier(CacheRequest request) {
        List<List<OrderInput>> groups =
                RouteCompatibilityUtil.groupByCompatibility(request.sortedOrders());

        return computeGlobalPareto(groups,
                request.truck().maxWeightLbs(),
                request.truck().maxVolumeCuft());
    }

    public String algorithmName() {
        return ALGORITHM_NAME;
    }

    private List<SolverResult> computeGlobalPareto(
            List<List<OrderInput>> groups,
            int maxWeightLbs,
            int maxVolumeCuft) {

        List<SolverResult> allGroupSolutions = new ArrayList<>();
        for (List<OrderInput> group : groups) {
            List<SolverResult> groupPareto = solver.solvePareto(group, maxWeightLbs, maxVolumeCuft);
            allGroupSolutions.addAll(groupPareto);
        }

        return ParetoUtil.computeFrontier(allGroupSolutions, maxWeightLbs, maxVolumeCuft);
    }
}
