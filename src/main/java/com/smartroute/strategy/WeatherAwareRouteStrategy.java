package com.smartroute.strategy;
import com.smartroute.exception.NoSuitableRouteException;
import com.smartroute.model.DeliveryRequest;
import com.smartroute.model.Route;
import com.smartroute.planner.RouteConstraints;
import java.util.Comparator;
import java.util.List;

public class WeatherAwareRouteStrategy implements RouteStrategy {

    private final int weatherSeverity;

    public WeatherAwareRouteStrategy(int weatherSeverity) {
        if (weatherSeverity < 0 || weatherSeverity > 10) {
            throw new IllegalArgumentException(
                    "Weather severity must be between 0 and 10"
            );
        }

        this.weatherSeverity = weatherSeverity;
    }

    private double calculateWeatherScore(Route route) {
        double riskPenalty =
                route.weatherRisk() * weatherSeverity * 2.0;

        double trafficPenalty =
                route.trafficLevel() * 1.0;

        return RouteConstraints.calculateEffectiveTime(route)
                + riskPenalty
                + trafficPenalty;
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
                        .<Route>comparingDouble(this::calculateWeatherScore)
                        .thenComparingInt(
                                RouteConstraints::calculateEffectiveTime)
                        .thenComparing(Route::id))
                .orElseThrow(() ->
                        new NoSuitableRouteException(
                                "No suitable weather-safe route found"
                        ));
    }

    @Override
    public String getStrategyName() {
        return "Weather-Aware Route Strategy";
    }
}
