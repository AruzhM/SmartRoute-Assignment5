package com.smartroute.model;
import java.util.List;
import java.util.Objects;

public record RouteDecision(
        Route selectedRoute,
        String strategyName,
        String explanation,
        double totalCost,
        int effectiveMinutes,
        List<String> rejectedRoutes
) {
    public RouteDecision {
        Objects.requireNonNull(selectedRoute);
        Objects.requireNonNull(strategyName);
        Objects.requireNonNull(explanation);
        Objects.requireNonNull(rejectedRoutes);

        rejectedRoutes = List.copyOf(rejectedRoutes);
    }
}
