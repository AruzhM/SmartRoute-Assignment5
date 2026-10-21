package com.smartroute.strategy;
import com.smartroute.exception.NoSuitableRouteException;
import com.smartroute.model.DeliveryRequest;
import com.smartroute.model.Route;
import com.smartroute.planner.RouteConstraints;
import java.util.Comparator;
import java.util.List;

public class BalancedRouteStrategy implements RouteStrategy {

    private final double timeWeight;
    private final double costWeight;
    private final double distanceWeight;
    private final double emissionWeight;

    public BalancedRouteStrategy(
            double timeWeight,
            double costWeight,
            double distanceWeight,
            double emissionWeight
    ) {
        double sum = timeWeight + costWeight
                + distanceWeight + emissionWeight;

        if (!isValidWeight(timeWeight)
                || !isValidWeight(costWeight)
                || !isValidWeight(distanceWeight)
                || !isValidWeight(emissionWeight)
                || Math.abs(sum - 1.0) > 0.000001) {
            throw new IllegalArgumentException(
                    "Invalid weights: they must be between 0 and 1 "
                            + "and sum to 1.0"
            );
        }

        this.timeWeight = timeWeight;
        this.costWeight = costWeight;
        this.distanceWeight = distanceWeight;
        this.emissionWeight = emissionWeight;
    }

    private boolean isValidWeight(double weight) {
        return Double.isFinite(weight)
                && weight >= 0
                && weight <= 1;
    }

    private double normalize(double value, double maximum) {
        return maximum == 0 ? 0 : value / maximum;
    }

    @Override
    public Route selectRoute(
            List<Route> routes,
            DeliveryRequest request
    ) {
        List<Route> validRoutes =
                RouteConstraints.filterValidRoutes(routes, request);

        if (validRoutes.isEmpty()) {
            throw new NoSuitableRouteException(
                    "No suitable route found for Balanced Strategy"
            );
        }

        double maxTime = validRoutes.stream()
                .mapToDouble(RouteConstraints::calculateEffectiveTime)
                .max().orElse(0);

        double maxCost = validRoutes.stream()
                .mapToDouble(route ->
                        RouteConstraints.calculateTotalCost(route, request))
                .max().orElse(0);

        double maxDistance = validRoutes.stream()
                .mapToDouble(Route::distanceKm)
                .max().orElse(0);

        double maxEmission = validRoutes.stream()
                .mapToDouble(Route::carbonEmission)
                .max().orElse(0);

        return validRoutes.stream()
                .min(Comparator
                        .<Route>comparingDouble(route ->
                                normalize(
                                        RouteConstraints.calculateEffectiveTime(route),
                                        maxTime) * timeWeight
                                        + normalize(
                                        RouteConstraints.calculateTotalCost(route, request),
                                        maxCost) * costWeight
                                        + normalize(
                                        route.distanceKm(),
                                        maxDistance) * distanceWeight
                                        + normalize(
                                        route.carbonEmission(),
                                        maxEmission) * emissionWeight)
                        .thenComparing(Route::id))
                .orElseThrow();
    }

    @Override
    public String getStrategyName() {
        return "Balanced Route Strategy";
    }
}
