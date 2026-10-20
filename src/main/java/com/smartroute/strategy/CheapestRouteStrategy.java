package com.smartroute.strategy;
import com.smartroute.model.DeliveryPriority;
import com.smartroute.model.DeliveryRequest;
import com.smartroute.model.Route;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class CheapestRouteStrategy implements RouteStrategy {

    private static final double COST_PER_KM = 50.0;
    private static final double URGENT_SURCHARGE = 500.0;
    private static final double TRAFFIC_FEE = 20.0;

    private double calculateTotalCost(
            Route route,
            DeliveryRequest request
    ) {
        double distanceCost =
                route.distanceKm() * COST_PER_KM;

        double prioritySurcharge =
                request.priority() == DeliveryPriority.URGENT
                        ? URGENT_SURCHARGE
                        : 0.0;

        double additionalFees =
                route.trafficLevel() * TRAFFIC_FEE;

        return route.cost()
                + distanceCost
                + prioritySurcharge
                + additionalFees;
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
                        calculateTotalCost(route, request)
                                <= request.maxBudget())
                .filter(route ->
                        calculateEffectiveTime(route)
                                <= request.maxDeliveryMinutes())

                .min(Comparator
                        .<Route>comparingDouble(
                                route -> calculateTotalCost(route, request))
                        .thenComparingInt(this::calculateEffectiveTime)
                        .thenComparing(Route::id))

                .orElseThrow(() ->
                        new IllegalStateException(
                                "No suitable route found for Cheapest Strategy"
                        ));
    }

    @Override
    public String getStrategyName() {
        return "Cheapest Route Strategy";
    }
}
