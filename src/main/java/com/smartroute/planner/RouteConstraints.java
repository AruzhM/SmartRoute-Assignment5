package com.smartroute.planner;
import com.smartroute.model.DeliveryPriority;
import com.smartroute.model.DeliveryRequest;
import com.smartroute.model.Route;
import java.util.List;
import java.util.Objects;

public class RouteConstraints {

    private static final double COST_PER_KM = 50.0;
    private static final double URGENT_SURCHARGE = 500.0;
    private static final double TRAFFIC_FEE = 20.0;

    private RouteConstraints() {
    }

    public static double calculateTotalCost(
            Route route,
            DeliveryRequest request
    ) {
        double distanceCost = route.distanceKm() * COST_PER_KM;

        double prioritySurcharge =
                request.priority() == DeliveryPriority.URGENT
                        ? URGENT_SURCHARGE
                        : 0.0;

        double trafficFee = route.trafficLevel() * TRAFFIC_FEE;

        return route.cost()
                + distanceCost
                + prioritySurcharge
                + trafficFee;
    }

    public static int calculateEffectiveTime(Route route) {
        return route.estimatedMinutes()
                + route.trafficLevel() * 2;
    }

    public static boolean isValid(
            Route route,
            DeliveryRequest request
    ) {
        Objects.requireNonNull(route, "Route cannot be null");
        Objects.requireNonNull(request, "Request cannot be null");

        return route.available()
                && calculateTotalCost(route, request)
                <= request.maxBudget()
                && calculateEffectiveTime(route)
                <= request.maxDeliveryMinutes();
    }

    public static List<Route> filterValidRoutes(
            List<Route> routes,
            DeliveryRequest request
    ) {
        Objects.requireNonNull(routes, "Routes cannot be null");
        Objects.requireNonNull(request, "Request cannot be null");

        return routes.stream()
                .filter(route -> isValid(route, request))
                .toList();
    }
}
