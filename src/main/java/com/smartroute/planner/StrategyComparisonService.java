package com.smartroute.planner;
import com.smartroute.exception.NoSuitableRouteException;
import com.smartroute.model.DeliveryRequest;
import com.smartroute.model.Route;
import com.smartroute.model.RouteDecision;
import com.smartroute.strategy.RouteStrategy;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class StrategyComparisonService {

    private final RouteDecisionService decisionService =
            new RouteDecisionService();

    public List<RouteDecision> compare(
            List<RouteStrategy> strategies,
            List<Route> routes,
            DeliveryRequest request
    ) {
        Objects.requireNonNull(strategies);
        Objects.requireNonNull(routes);
        Objects.requireNonNull(request);

        List<RouteDecision> results = new ArrayList<>();

        for (RouteStrategy strategy : strategies) {
            try {
                RouteDecision decision =
                        decisionService.explain(
                                strategy, routes, request
                        );

                results.add(decision);

            } catch (NoSuitableRouteException exception) {
                System.out.println(
                        strategy.getStrategyName()
                                + ": " + exception.getMessage()
                );
            }
        }

        return List.copyOf(results);
    }

    public void printComparison(
            List<RouteDecision> decisions
    ) {
        System.out.println();
        System.out.println("================================");
        System.out.println("      STRATEGY COMPARISON");
        System.out.println("================================");

        for (RouteDecision decision : decisions) {
            System.out.println();
            System.out.println("Strategy: "
                    + decision.strategyName());

            System.out.println("Selected route: "
                    + decision.selectedRoute().id());

            System.out.println("Effective time: "
                    + decision.effectiveMinutes() + " min");

            System.out.println("Total cost: "
                    + decision.totalCost() + " KZT");

            System.out.println("Distance: "
                    + decision.selectedRoute().distanceKm() + " km");

            System.out.println("CO2 emissions: "
                    + decision.selectedRoute().carbonEmission() + " kg");

            System.out.println("--------------------------------");
        }
    }
}
