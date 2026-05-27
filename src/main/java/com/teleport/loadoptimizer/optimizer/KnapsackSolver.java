package com.teleport.loadoptimizer.optimizer;

import com.teleport.loadoptimizer.model.request.OrderInput;

import java.util.List;
import java.util.Optional;

public interface KnapsackSolver {

    Optional<SolverResult> solve(List<OrderInput> candidates, int maxWeightLbs, int maxVolumeCuft);

    List<SolverResult> solvePareto(List<OrderInput> candidates, int maxWeightLbs, int maxVolumeCuft);
}
