package com.smartroute.strategy;
import com.smartroute.exception.NoSuitableRouteException;
import com.smartroute.model.DeliveryRequest;
import com.smartroute.model.Route;
import com.smartroute.planner.RouteConstraints;
import java.util.Comparator;
import java.util.List;

public class CheapestRouteStrategy implements RouteStrategy {

    @Override
    public Route selectRoute(
            List<Route> routes,
            DeliveryRequest request
    ) {
        List<Route> validRoutes =
                RouteConstraints.filterValidRoutes(routes, request);

        return validRoutes.stream()
                .min(Comparator
                        .<Route>comparingDouble(route ->
                                RouteConstraints.calculateTotalCost(
                                        route, request))
                        .thenComparingInt(
                                RouteConstraints::calculateEffectiveTime)
                        .thenComparing(Route::id))
                .orElseThrow(() ->
                        new NoSuitableRouteException(
                                "No suitable route found for Cheapest Strategy"
                        ));
    }

    @Override
    public String getStrategyName() {
        return "Cheapest Route Strategy";
    }
}
