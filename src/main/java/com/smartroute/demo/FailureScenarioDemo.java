package com.smartroute.demo;
import com.smartroute.exception.NoSuitableRouteException;
import com.smartroute.model.DeliveryPriority;
import com.smartroute.model.DeliveryRequest;
import com.smartroute.model.Route;
import com.smartroute.strategy.BalancedRouteStrategy;
import com.smartroute.strategy.FastestRouteStrategy;
import com.smartroute.strategy.RouteStrategy;

import java.util.List;

public class FailureScenarioDemo {

    public static void run(List<Route> routes) {

        System.out.println();
        System.out.println("================================");
        System.out.println("       FAILURE SCENARIOS");
        System.out.println("================================");

        testEmptyRoutes();
        testInsufficientBudget(routes);
        testInvalidWeights();
        testUnavailableRoutes(routes);
    }

    private static void testEmptyRoutes() {
        try {
            RouteStrategy strategy = new FastestRouteStrategy();

            DeliveryRequest request = new DeliveryRequest(
                    DeliveryPriority.STANDARD, 6000, 45, false
            );

            strategy.selectRoute(List.of(), request);

        } catch (NoSuitableRouteException exception) {
            printFailure("Empty route list", exception);
        }
    }

    private static void testInsufficientBudget(List<Route> routes) {
        try {
            DeliveryRequest request = new DeliveryRequest(
                    DeliveryPriority.ECONOMY, 100, 45, false
            );

            new FastestRouteStrategy().selectRoute(routes, request);

        } catch (NoSuitableRouteException exception) {
            printFailure("Insufficient budget", exception);
        }
    }

    private static void testInvalidWeights() {
        try {
            new BalancedRouteStrategy(0.5, 0.5, 0.5, 0.5);

        } catch (IllegalArgumentException exception) {
            printFailure("Invalid strategy weights", exception);
        }
    }

    private static void testUnavailableRoutes(List<Route> routes) {
        try {
            List<Route> unavailable = routes.stream()
                    .filter(route -> !route.available())
                    .toList();

            DeliveryRequest request = new DeliveryRequest(
                    DeliveryPriority.STANDARD, 6000, 45, false
            );

            new FastestRouteStrategy().selectRoute(unavailable, request);

        } catch (NoSuitableRouteException exception) {
            printFailure("Unavailable routes", exception);
        }
    }

    private static void printFailure(
            String scenario,
            Exception exception
    ) {
        System.out.println();
        System.out.println("Scenario: " + scenario);
        System.out.println("Handled: " + exception.getClass().getSimpleName());
        System.out.println("Reason: " + exception.getMessage());
    }
}
