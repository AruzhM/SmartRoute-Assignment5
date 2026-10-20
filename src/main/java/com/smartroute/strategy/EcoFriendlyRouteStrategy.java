package com.smartroute.strategy;
import com.smartroute.model.DeliveryRequest;
import com.smartroute.model.Route;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class EcoFriendlyRouteStrategy implements RouteStrategy {

    private static final double EMISSION_WEIGHT = 0.8;
    private static final double DISTANCE_WEIGHT = 0.2;

    private double calculateEcoScore(Route route) {
        return route.carbonEmission() * EMISSION_WEIGHT
                + route.distanceKm() * DISTANCE_WEIGHT;
    }

    private int calculateEffectiveTime(Route route) {
        return route.estimatedMinutes()
                + route.trafficLevel() * 2;
    }

    @Override
    public Route selectRoute(
            List<Route> routes,
            DeliveryRequest request
    ) {
        Objects.requireNonNull(routes, "Routes cannot be null");
        Objects.requireNonNull(request, "Request cannot be null");

        return routes.stream()
                .filter(Route::available)
                .filter(route ->
                        route.cost() <= request.maxBudget())
                .filter(route ->
                        calculateEffectiveTime(route)
                                <= request.maxDeliveryMinutes())
                .min(Comparator
                        .<Route>comparingDouble(this::calculateEcoScore)
                        .thenComparingInt(this::calculateEffectiveTime)
                        .thenComparing(Route::id))
                .orElseThrow(() ->
                        new IllegalStateException(
                                "No suitable route found for Eco Strategy"
                        ));
    }

    @Override
    public String getStrategyName() {
        return "Eco-Friendly Route Strategy";
    }
}
