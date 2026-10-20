package com.smartroute.selector;
import com.smartroute.model.DeliveryPriority;
import com.smartroute.model.DeliveryRequest;
import com.smartroute.strategy.*;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public class StrategySelector {

    private final Map<DeliveryPriority, RouteStrategy> strategies =
            new EnumMap<>(DeliveryPriority.class);

    public StrategySelector() {
        registerStrategy(
                DeliveryPriority.URGENT,
                new FastestRouteStrategy()
        );

        registerStrategy(
                DeliveryPriority.ECONOMY,
                new CheapestRouteStrategy()
        );

        registerStrategy(
                DeliveryPriority.ECO,
                new EcoFriendlyRouteStrategy()
        );

        registerStrategy(
                DeliveryPriority.STANDARD,
                new BalancedRouteStrategy(
                        0.40, 0.30, 0.10, 0.20
                )
        );
    }

    public void registerStrategy(
            DeliveryPriority priority,
            RouteStrategy strategy
    ) {
        strategies.put(
                Objects.requireNonNull(priority),
                Objects.requireNonNull(strategy)
        );
    }

    public RouteStrategy selectStrategy(
            DeliveryRequest request
    ) {
        Objects.requireNonNull(request, "Request cannot be null");

        RouteStrategy selected = strategies.get(request.priority());

        if (selected == null) {
            throw new IllegalArgumentException(
                    "No strategy registered for priority: "
                            + request.priority()
            );
        }

        return selected;
    }
}
