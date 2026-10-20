package com.smartroute.strategy;
import com.smartroute.model.DeliveryRequest;
import com.smartroute.model.Route;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class FastestRouteStrategy implements RouteStrategy {

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
                        .comparingInt(this::calculateEffectiveTime)
                        .thenComparingDouble(Route::cost)
                        .thenComparing(Route::id))
                .orElseThrow(() ->
                        new IllegalStateException(
                                "No suitable route found for Fastest Strategy"
                        ));
    }

    @Override
    public String getStrategyName() {
        return "Fastest Route Strategy";
    }
}
