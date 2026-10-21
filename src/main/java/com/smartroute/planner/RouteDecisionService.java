package com.smartroute.planner;
import com.smartroute.model.DeliveryRequest;
import com.smartroute.model.Route;
import com.smartroute.model.RouteDecision;
import com.smartroute.strategy.RouteStrategy;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class RouteDecisionService {

    public RouteDecision explain(
            RouteStrategy strategy,
            List<Route> routes,
            DeliveryRequest request
    ) {
        Objects.requireNonNull(strategy);
        Objects.requireNonNull(routes);
        Objects.requireNonNull(request);

        Route selected = strategy.selectRoute(routes, request);

        List<String> rejected = new ArrayList<>();

        for (Route route : routes) {

            if (route.equals(selected)) {
                continue;
            }

            if (!route.available()) {
                rejected.add(route.id() + ": Route unavailable");
            } else if (RouteConstraints.calculateTotalCost(route, request)
                    > request.maxBudget()) {
                rejected.add(route.id() + ": Budget exceeded");
            } else if (RouteConstraints.calculateEffectiveTime(route)
                    > request.maxDeliveryMinutes()) {
                rejected.add(route.id() + ": Deadline exceeded");
            } else {
                rejected.add(route.id()
                        + ": Valid alternative, not selected");
            }
        }

        String explanation = buildExplanation(
                strategy, selected, routes, request
        );

        return new RouteDecision(
                selected,
                strategy.getStrategyName(),
                explanation,
                RouteConstraints.calculateTotalCost(selected, request),
                RouteConstraints.calculateEffectiveTime(selected),
                rejected
        );
    }

    private String buildExplanation(
            RouteStrategy strategy,
            Route selected,
            List<Route> routes,
            DeliveryRequest request
    ) {
        List<Route> alternatives = RouteConstraints
                .filterValidRoutes(routes, request)
                .stream()
                .filter(route -> !route.equals(selected))
                .toList();

        StringBuilder explanation = new StringBuilder();

        explanation.append("Selected ")
                .append(selected.id())
                .append(" using ")
                .append(strategy.getStrategyName())
                .append(".\n");

        explanation.append("The route satisfies all hard constraints.\n");

        if (strategy.getStrategyName().equals("Fastest Route Strategy")) {

            int selectedTime =
                    RouteConstraints.calculateEffectiveTime(selected);

            explanation.append("Effective delivery time: ")
                    .append(selectedTime)
                    .append(" minutes.\n");

            alternatives.stream()
                    .min(Comparator.comparingInt(
                            RouteConstraints::calculateEffectiveTime))
                    .ifPresent(other -> {
                        int difference =
                                RouteConstraints.calculateEffectiveTime(other)
                                        - selectedTime;

                        explanation.append("It is ")
                                .append(difference)
                                .append(" minute(s) faster than ")
                                .append(other.id())
                                .append(".\n");
                    });

        } else if (strategy.getStrategyName()
                .equals("Cheapest Route Strategy")) {

            double selectedCost =
                    RouteConstraints.calculateTotalCost(selected, request);

            explanation.append("Total delivery cost: ")
                    .append(selectedCost)
                    .append(" KZT.\n");

            alternatives.stream()
                    .min(Comparator.comparingDouble(route ->
                            RouteConstraints.calculateTotalCost(route, request)))
                    .ifPresent(other -> {
                        double difference =
                                RouteConstraints.calculateTotalCost(other, request)
                                        - selectedCost;

                        explanation.append("It saves ")
                                .append(String.format("%.2f", difference))
                                .append(" KZT compared to ")
                                .append(other.id())
                                .append(".\n");
                    });

        } else if (strategy.getStrategyName()
                .equals("Eco-Friendly Route Strategy")) {

            explanation.append("Carbon emissions: ")
                    .append(selected.carbonEmission())
                    .append(" kg.\n");

            explanation.append(
                    "Selected using weighted carbon emission "
                            + "and distance optimization.\n"
            );

        } else if (strategy.getStrategyName()
                .equals("Balanced Route Strategy")) {

            explanation.append(
                    "Selected using normalized time, cost, "
                            + "distance and emission criteria.\n"
            );
        }

        return explanation.toString();
    }
}
