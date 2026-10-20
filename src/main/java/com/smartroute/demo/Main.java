package com.smartroute.demo;
import com.smartroute.model.*;
import com.smartroute.planner.RoutePlanner;
import com.smartroute.strategy.*;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        Location warehouse = new Location(
                "Astana Warehouse", 51.1694, 71.4491
        );

        Location customer = new Location(
                "Astana Customer", 51.1282, 71.4304
        );

        List<Route> routes = List.of(
                new Route("R-01", warehouse, customer,
                        8.5, 20, 3500, 2.4, true, 7, 3),

                new Route("R-02", warehouse, customer,
                        10.2, 25, 2800, 1.8, true, 2, 2),

                new Route("R-03", warehouse, customer,
                        12.0, 28, 2500, 1.2, true, 1, 1),

                new Route("R-04", warehouse, customer,
                        7.8, 23, 4200, 3.1, true, 5, 4),

                new Route("R-05", warehouse, customer,
                        14.5, 35, 2200, 1.0, true, 0, 1),

                new Route("R-06", warehouse, customer,
                        9.0, 22, 3900, 2.0, true, 4, 6),

                new Route("R-07", warehouse, customer,
                        11.3, 30, 2700, 1.5, false, 2, 2),

                new Route("R-08", warehouse, customer,
                        13.0, 32, 2400, 1.3, true, 3, 5)
        );

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

        // Runtime switch 1
        planner.setStrategy(new CheapestRouteStrategy());
        displayResult(planner, routes, request);

        // Runtime switch 2
        planner.setStrategy(new EcoFriendlyRouteStrategy());
        displayResult(planner, routes, request);

        // Runtime switch 3
        planner.setStrategy(new BalancedRouteStrategy(
                0.40, 0.30, 0.10, 0.20
        ));
        displayResult(planner, routes, request);
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
        System.out.println("Selected route: " + selected.id());
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
