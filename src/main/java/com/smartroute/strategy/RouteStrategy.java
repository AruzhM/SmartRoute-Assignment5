package com.smartroute.strategy;

import com.smartroute.model.DeliveryRequest;
import com.smartroute.model.Route;

import java.util.List;

public interface RouteStrategy {

    Route selectRoute(
            List<Route> routes,
            DeliveryRequest request
    );

    String getStrategyName();
}
