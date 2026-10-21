package com.smartroute.strategy;
import com.smartroute.exception.NoSuitableRouteException;
import com.smartroute.model.DeliveryRequest;
import com.smartroute.model.Route;
import com.smartroute.planner.RouteConstraints;
import java.util.Comparator;
import java.util.List;

public class FastestRouteStrategy implements RouteStrategy {

    @Override
    public Route selectRoute(
            List<Route> routes,
            DeliveryRequest request
    ) {
        List<Route> validRoutes =
                RouteConstraints.filterValidRoutes(routes, request);

        return validRoutes.stream()
                .min(Comparator
                        .comparingInt(RouteConstraints::calculateEffectiveTime)
                        .thenComparingDouble(route ->
                                RouteConstraints.calculateTotalCost(
                                        route, request))
                        .thenComparing(Route::id))
                .orElseThrow(() ->
                        new NoSuitableRouteException(
                                "No suitable route found for Fastest Strategy"
                        ));
    }

    @Override
    public String getStrategyName() {
        return "Fastest Route Strategy";
    }
}
