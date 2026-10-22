package com.smartroute.demo;
import com.smartroute.model.*;
import com.smartroute.planner.RoutePlanner;
import com.smartroute.planner.RouteDecisionService;
import com.smartroute.selector.StrategySelector;
import com.smartroute.strategy.*;
import java.util.List;
import com.smartroute.planner.StrategyComparisonService;
import com.smartroute.planner.RouteConstraints;

public class Main {

    public static void main(String[] args) {

        List<Route> routes = DemoData.createRoutes();

        DeliveryRequest request = new DeliveryRequest(
                DeliveryPriority.STANDARD,
                6000,
                45,
                false
        );

        RoutePlanner planner = new RoutePlanner(
                new FastestRouteStrategy()
        );

        System.out.println("================================");
        System.out.println("   SMARTROUTE DELIVERY ENGINE");
        System.out.println("================================");

        displayResult(planner, routes, request);

        planner.setStrategy(new CheapestRouteStrategy());
        displayResult(planner, routes, request);

        planner.setStrategy(new EcoFriendlyRouteStrategy());
        displayResult(planner, routes, request);

        planner.setStrategy(new BalancedRouteStrategy(
                0.40, 0.30, 0.10, 0.20
        ));
        displayResult(planner, routes, request);

        System.out.println();
        System.out.println("================================");
        System.out.println("   AUTOMATIC STRATEGY SELECTION");
        System.out.println("================================");

        StrategySelector selector = new StrategySelector();

        for (DeliveryPriority priority : DeliveryPriority.values()) {

            DeliveryRequest automaticRequest = new DeliveryRequest(
                    priority,
                    6000,
                    45,
                    priority == DeliveryPriority.ECO
            );

            RouteStrategy selectedStrategy =
                    selector.selectStrategy(automaticRequest);

            planner.setStrategy(selectedStrategy);

            System.out.println();
            System.out.println("Delivery priority: " + priority);

            displayResult(planner, routes, automaticRequest);
        }

        System.out.println();
        System.out.println("================================");
        System.out.println("     SMART DECISION REPORT");
        System.out.println("================================");

        RouteDecisionService decisionService =
                new RouteDecisionService();

        DeliveryRequest reportRequest = new DeliveryRequest(
                DeliveryPriority.URGENT,
                4000,
                35,
                false
        );

        RouteDecision decision = decisionService.explain(
                new FastestRouteStrategy(),
                routes,
                reportRequest
        );

        System.out.println("Selected route: "
                + decision.selectedRoute().id());

        System.out.println("Strategy: "
                + decision.strategyName());

        System.out.println();
        System.out.println("WHY THIS ROUTE?");
        System.out.println(decision.explanation());

        System.out.println("Effective time: "
                + decision.effectiveMinutes() + " min");

        System.out.println("Total cost: "
                + decision.totalCost() + " KZT");

        System.out.println();
        System.out.println("OTHER ROUTES:");

        for (String reason : decision.rejectedRoutes()) {
            System.out.println("- " + reason);
        }

        StrategyComparisonService comparisonService =
                new StrategyComparisonService();

        List<RouteStrategy> comparisonStrategies = List.of(
                new FastestRouteStrategy(),
                new CheapestRouteStrategy(),
                new EcoFriendlyRouteStrategy(),
                new BalancedRouteStrategy(0.40, 0.30, 0.10, 0.20),
                new WeatherAwareRouteStrategy(7)
        );


        List<RouteDecision> comparisonResults =
                comparisonService.compare(
                        comparisonStrategies,
                        routes,
                        request
                );

        comparisonService.printComparison(comparisonResults);


        System.out.println();
        System.out.println("================================");
        System.out.println("      WEATHER SIMULATION");
        System.out.println("================================");

        for (int severity : new int[]{0, 3, 7, 10}) {

            planner.setStrategy(
                    new WeatherAwareRouteStrategy(severity)
            );

            System.out.println();
            System.out.println("Weather severity: " + severity + "/10");

            Route selected = planner.plan(routes, request);

            double weatherScore =
                    RouteConstraints.calculateEffectiveTime(selected)
                            + selected.weatherRisk() * severity * 2.0
                            + selected.trafficLevel() * 1.0;

            System.out.println("Selected route: " + selected.id());
            System.out.println("Weather risk: " + selected.weatherRisk());
            System.out.printf("Weather score: %.2f%n", weatherScore);
        }

        FailureScenarioDemo.run(routes);

    }



    private static void displayResult(
            RoutePlanner planner,
            List<Route> routes,
            DeliveryRequest request
    ) {
        Route selected = planner.plan(routes, request);

        System.out.println();
        System.out.println("Strategy: "
                + planner.getCurrentStrategyName());

        System.out.println("Selected route: "
                + selected.id());

        System.out.println("Estimated time: "
                + selected.estimatedMinutes() + " min");

        System.out.println("Base cost: "
                + selected.cost() + " KZT");

        System.out.println("Distance: "
                + selected.distanceKm() + " km");

        System.out.println("CO2 emissions: "
                + selected.carbonEmission() + " kg");

        System.out.println("--------------------------------");
    }
}
