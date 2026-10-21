package com.smartroute.planner;
import com.smartroute.model.DeliveryRequest;
import com.smartroute.model.Route;
import com.smartroute.strategy.RouteStrategy;
import java.util.List;
import java.util.Objects;

public class RoutePlanner {

    private RouteStrategy strategy;

    public RoutePlanner(RouteStrategy strategy) {
        setStrategy(strategy);
    }

    public void setStrategy(RouteStrategy strategy) {
        this.strategy = Objects.requireNonNull(
                strategy,
                "Route strategy cannot be null"
        );
    }

    public Route plan(
            List<Route> routes,
            DeliveryRequest request
    ) {
        return strategy.selectRoute(routes, request);
    }

    public String getCurrentStrategyName() {
        return strategy.getStrategyName();
    }
}
