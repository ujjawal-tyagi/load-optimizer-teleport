package com.teleport.loadoptimizer.optimizer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class ParetoUtil {

    private ParetoUtil() {}

    public static List<SolverResult> computeFrontier(
            List<SolverResult> solutions,
            int maxWeightLbs,
            int maxVolumeCuft) {

        if (solutions == null || solutions.isEmpty()) {
            return List.of();
        }

        List<SolverResult> sorted = solutions.stream()
                .sorted(Comparator
                        .comparingLong(SolverResult::totalPayoutCents).reversed()
                        .thenComparingInt(s ->
                                -combinedUtilizationPermille(s, maxWeightLbs, maxVolumeCuft)))
                .toList();

        List<SolverResult> frontier = new ArrayList<>();
        int maxUtilSeen = -1;

        for (SolverResult candidate : sorted) {
            int util = combinedUtilizationPermille(candidate, maxWeightLbs, maxVolumeCuft);
            if (util > maxUtilSeen) {
                frontier.add(candidate);
                maxUtilSeen = util;
            }

        }

        return List.copyOf(frontier);
    }

    public static int combinedUtilizationPermille(
            SolverResult s, int maxWeightLbs, int maxVolumeCuft) {
        int weightPermille = maxWeightLbs > 0
                ? 1000 * s.totalWeightLbs() / maxWeightLbs : 0;
        int volumePermille = maxVolumeCuft > 0
                ? 1000 * s.totalVolumeCuft() / maxVolumeCuft : 0;
        return (weightPermille + volumePermille) / 2;
    }
}
