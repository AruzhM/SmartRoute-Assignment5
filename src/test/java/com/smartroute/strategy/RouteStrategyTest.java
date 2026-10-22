package com.smartroute.strategy;
import com.smartroute.model.*;
import com.smartroute.planner.RouteConstraints;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import com.smartroute.planner.RoutePlanner;
import com.smartroute.planner.StrategyComparisonService;
import com.smartroute.selector.StrategySelector;

class RouteStrategyTest {

    private List<Route> routes;
    private DeliveryRequest request;

    @BeforeEach
    void setUp() {
        Location warehouse = new Location(
                "Warehouse", 51.1694, 71.4491
        );

        Location customer = new Location(
                "Customer", 51.1282, 71.4304
        );

        routes = List.of(
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

        request = new DeliveryRequest(
                DeliveryPriority.STANDARD,
                6000,
                45,
                false
        );
    }

    @Test
    void fastestStrategySelectsMinimumEffectiveTime() {
        Route selected =
                new FastestRouteStrategy().selectRoute(routes, request);

        assertEquals("R-02", selected.id());
        assertEquals(29,
                RouteConstraints.calculateEffectiveTime(selected));
    }

    @Test
    void cheapestStrategySelectsMinimumTotalCost() {
        Route selected =
                new CheapestRouteStrategy().selectRoute(routes, request);

        assertEquals("R-05", selected.id());
        assertEquals(2925.0,
                RouteConstraints.calculateTotalCost(selected, request),
                0.001);
    }

    @Test
    void ecoStrategySelectsLowestEcoScore() {
        Route selected =
                new EcoFriendlyRouteStrategy().selectRoute(routes, request);

        assertEquals("R-03", selected.id());
    }

    @Test
    void balancedStrategySelectsWeightedOptimalRoute() {
        Route selected = new BalancedRouteStrategy(
                0.40, 0.30, 0.10, 0.20
        ).selectRoute(routes, request);

        assertEquals("R-03", selected.id());
    }

    @Test
    void weatherStrategyChangesWithSeverity() {
        Route clearWeather =
                new WeatherAwareRouteStrategy(0)
                        .selectRoute(routes, request);

        Route severeWeather =
                new WeatherAwareRouteStrategy(7)
                        .selectRoute(routes, request);

        assertEquals("R-02", clearWeather.id());
        assertEquals("R-03", severeWeather.id());
        assertNotEquals(clearWeather.id(), severeWeather.id());
    }

    @Test
    void routeExceedingBudgetIsRejected() {
        DeliveryRequest limitedBudget = new DeliveryRequest(
                DeliveryPriority.STANDARD, 3000, 45, false
        );

        Route selected = new CheapestRouteStrategy()
                .selectRoute(routes, limitedBudget);

        assertEquals("R-05", selected.id());
        assertTrue(RouteConstraints.calculateTotalCost(
                selected, limitedBudget) <= 3000);
    }

    @Test
    void routeExceedingDeadlineIsRejected() {
        DeliveryRequest shortDeadline = new DeliveryRequest(
                DeliveryPriority.URGENT, 6000, 29, false
        );

        Route selected = new FastestRouteStrategy()
                .selectRoute(routes, shortDeadline);

        assertEquals("R-02", selected.id());
        assertTrue(RouteConstraints.calculateEffectiveTime(selected) <= 29);
    }

    @Test
    void unavailableRouteIsNeverSelected() {
        Route selected = new CheapestRouteStrategy()
                .selectRoute(routes, request);

        assertTrue(selected.available());
        assertNotEquals("R-07", selected.id());
    }

    @Test
    void invalidBalancedWeightsAreRejected() {
        assertThrows(IllegalArgumentException.class, () ->
                new BalancedRouteStrategy(0.5, 0.5, 0.5, 0.5)
        );
    }

    @Test
    void noSuitableRouteThrowsException() {
        DeliveryRequest impossibleRequest = new DeliveryRequest(
                DeliveryPriority.STANDARD, 100, 10, false
        );

        assertThrows(
                com.smartroute.exception.NoSuitableRouteException.class,
                () -> new FastestRouteStrategy()
                        .selectRoute(routes, impossibleRequest)
        );
    }


    @Test
    void runtimeStrategySwitchingWorks() {
        RoutePlanner planner = new RoutePlanner(
                new FastestRouteStrategy()
        );

        assertEquals("R-02",
                planner.plan(routes, request).id());

        planner.setStrategy(new CheapestRouteStrategy());

        assertEquals("R-05",
                planner.plan(routes, request).id());

        planner.setStrategy(new EcoFriendlyRouteStrategy());

        assertEquals("R-03",
                planner.plan(routes, request).id());
    }

    @Test
    void automaticStrategySelectionWorks() {
        StrategySelector selector = new StrategySelector();

        DeliveryRequest urgentRequest = new DeliveryRequest(
                DeliveryPriority.URGENT, 6000, 45, false
        );

        RouteStrategy selected =
                selector.selectStrategy(urgentRequest);

        assertInstanceOf(FastestRouteStrategy.class, selected);
        assertEquals("R-02",
                selected.selectRoute(routes, urgentRequest).id());
    }

    @Test
    void comparisonModeReturnsAllStrategies() {
        StrategyComparisonService comparison =
                new StrategyComparisonService();

        List<RouteStrategy> strategies = List.of(
                new FastestRouteStrategy(),
                new CheapestRouteStrategy(),
                new EcoFriendlyRouteStrategy(),
                new BalancedRouteStrategy(0.4, 0.3, 0.1, 0.2),
                new WeatherAwareRouteStrategy(7)
        );

        var results = comparison.compare(
                strategies, routes, request
        );

        assertEquals(5, results.size());
        assertEquals("R-02",
                results.get(0).selectedRoute().id());
        assertEquals("R-05",
                results.get(1).selectedRoute().id());
    }

    @Test
    void emptyRouteListThrowsException() {
        assertThrows(
                com.smartroute.exception.NoSuitableRouteException.class,
                () -> new FastestRouteStrategy()
                        .selectRoute(List.of(), request)
        );
    }

    @Test
    void routeExactlyAtBudgetLimitIsAccepted() {
        DeliveryRequest exactBudget = new DeliveryRequest(
                DeliveryPriority.STANDARD, 2925, 45, false
        );

        Route selected = new CheapestRouteStrategy()
                .selectRoute(routes, exactBudget);

        assertEquals("R-05", selected.id());
        assertEquals(2925.0,
                RouteConstraints.calculateTotalCost(
                        selected, exactBudget
                ), 0.001);
    }




}
