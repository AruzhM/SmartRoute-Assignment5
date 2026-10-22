package com.smartroute.demo;
import com.smartroute.exception.NoSuitableRouteException;
import com.smartroute.model.*;
import com.smartroute.planner.RouteConstraints;
import com.smartroute.planner.RoutePlanner;
import com.smartroute.selector.StrategySelector;
import com.smartroute.strategy.RouteStrategy;
import java.util.List;
import java.util.Scanner;

public class InteractiveDeliverySimulator {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        StrategySelector selector = new StrategySelector();
        List<Route> routes = DemoData.createRoutes();

        System.out.println("================================");
        System.out.println("    SMARTROUTE INTERACTIVE");
        System.out.println("================================");

        boolean running = true;

        while (running) {
            System.out.println();
            System.out.println("Choose delivery priority:");
            System.out.println("1. URGENT");
            System.out.println("2. ECONOMY");
            System.out.println("3. ECO");
            System.out.println("4. STANDARD");
            System.out.println("0. EXIT");
            System.out.print("Your choice: ");

            String choice = scanner.nextLine().trim();

            if (choice.equals("0")) {
                running = false;
                continue;
            }

            DeliveryPriority priority;

            switch (choice) {
                case "1" -> priority = DeliveryPriority.URGENT;
                case "2" -> priority = DeliveryPriority.ECONOMY;
                case "3" -> priority = DeliveryPriority.ECO;
                case "4" -> priority = DeliveryPriority.STANDARD;
                default -> {
                    System.out.println("Invalid choice. Try again.");
                    continue;
                }
            }

            try {
                System.out.print("Enter budget (KZT): ");
                double budget = Double.parseDouble(
                        scanner.nextLine().trim()
                );

                System.out.print("Enter deadline (minutes): ");
                int deadline = Integer.parseInt(
                        scanner.nextLine().trim()
                );

                DeliveryRequest request = new DeliveryRequest(
                        priority,
                        budget,
                        deadline,
                        priority == DeliveryPriority.ECO
                );

                RouteStrategy strategy =
                        selector.selectStrategy(request);

                RoutePlanner planner = new RoutePlanner(strategy);
                Route selected = planner.plan(routes, request);

                System.out.println();
                System.out.println("--------------------------------");
                System.out.println("       RECOMMENDED ROUTE");
                System.out.println("--------------------------------");

                System.out.println("Strategy: "
                        + planner.getCurrentStrategyName());

                System.out.println("Route: " + selected.id());

                System.out.println("Effective time: "
                        + RouteConstraints.calculateEffectiveTime(selected)
                        + " min");

                System.out.printf("Total cost: %.2f KZT%n",
                        RouteConstraints.calculateTotalCost(
                                selected, request));

                System.out.println("Distance: "
                        + selected.distanceKm() + " km");

                System.out.println("CO2 emissions: "
                        + selected.carbonEmission() + " kg");

            } catch (NumberFormatException exception) {
                System.out.println(
                        "Invalid number. Please enter numeric values."
                );
            } catch (NoSuitableRouteException exception) {
                System.out.println(
                        "No suitable delivery route: "
                                + exception.getMessage()
                );
            } catch (IllegalArgumentException exception) {
                System.out.println(
                        "Invalid delivery request: "
                                + exception.getMessage()
                );
            }
        }

        System.out.println();
        System.out.println("Thank you for using SmartRoute!");
        scanner.close();
    }
}
