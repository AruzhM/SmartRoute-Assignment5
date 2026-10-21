package com.smartroute.strategy;
import com.smartroute.exception.NoSuitableRouteException;
import com.smartroute.model.DeliveryRequest;
import com.smartroute.model.Route;
import com.smartroute.planner.RouteConstraints;
import java.util.Comparator;
import java.util.List;

public class EcoFriendlyRouteStrategy implements RouteStrategy {

    private static final double EMISSION_WEIGHT = 0.8;
    private static final double DISTANCE_WEIGHT = 0.2;

    private double calculateEcoScore(Route route) {
        return route.carbonEmission() * EMISSION_WEIGHT
                + route.distanceKm() * DISTANCE_WEIGHT;
    }

    @Override
    public Route selectRoute(
            List<Route> routes,
            DeliveryRequest request
    ) {
        List<Route> validRoutes =
                RouteConstraints.filterValidRoutes(routes, request);

        return validRoutes.stream()
                .min(Comparator
                        .<Route>comparingDouble(this::calculateEcoScore)
                        .thenComparingInt(
                                RouteConstraints::calculateEffectiveTime)
                        .thenComparing(Route::id))
                .orElseThrow(() ->
                        new NoSuitableRouteException(
                                "No suitable route found for Eco Strategy"
                        ));
    }

    @Override
    public String getStrategyName() {
        return "Eco-Friendly Route Strategy";
    }
}
