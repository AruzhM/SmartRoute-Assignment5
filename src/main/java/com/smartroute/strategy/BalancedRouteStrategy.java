package com.smartroute.strategy;
import com.smartroute.model.DeliveryRequest;
import com.smartroute.model.Route;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

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

        if (!Double.isFinite(sum)
                || Math.abs(sum - 1.0) > 0.000001) {
            throw new IllegalArgumentException(
                    "Strategy weights must sum to 1.0"
            );
        }

        if (!isValidWeight(timeWeight)
                || !isValidWeight(costWeight)
                || !isValidWeight(distanceWeight)
                || !isValidWeight(emissionWeight)) {
            throw new IllegalArgumentException(
                    "Weights must be between 0 and 1"
            );
        }

        this.timeWeight = timeWeight;
        this.costWeight = costWeight;
        this.distanceWeight = distanceWeight;
        this.emissionWeight = emissionWeight;
    }

    private boolean isValidWeight(double weight) {
        return Double.isFinite(weight)
                && weight >= 0 && weight <= 1;
    }

    private int effectiveTime(Route route) {
        return route.estimatedMinutes()
                + route.trafficLevel() * 2;
    }

    private double normalized(double value, double maximum) {
        return maximum == 0 ? 0 : value / maximum;
    }

    @Override
    public Route selectRoute(
            List<Route> routes,
            DeliveryRequest request
    ) {
        Objects.requireNonNull(routes, "Routes cannot be null");
        Objects.requireNonNull(request, "Request cannot be null");

        List<Route> validRoutes = routes.stream()
                .filter(Route::available)
                .filter(route ->
                        route.cost() <= request.maxBudget())
                .filter(route ->
                        effectiveTime(route)
                                <= request.maxDeliveryMinutes())
                .toList();

        if (validRoutes.isEmpty()) {
            throw new IllegalStateException(
                    "No suitable route found for Balanced Strategy"
            );
        }

        double maxTime = validRoutes.stream()
                .mapToDouble(this::effectiveTime)
                .max().orElse(0);

        double maxCost = validRoutes.stream()
                .mapToDouble(Route::cost)
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
                                normalized(effectiveTime(route), maxTime)
                                        * timeWeight
                                        + normalized(route.cost(), maxCost)
                                        * costWeight
                                        + normalized(route.distanceKm(), maxDistance)
                                        * distanceWeight
                                        + normalized(route.carbonEmission(), maxEmission)
                                        * emissionWeight)
                        .thenComparing(Route::id))
                .orElseThrow();
    }

    @Override
    public String getStrategyName() {
        return "Balanced Route Strategy";
    }
}
