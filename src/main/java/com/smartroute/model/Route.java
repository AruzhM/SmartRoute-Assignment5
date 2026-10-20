package com.smartroute.model;

public record Route(
        String id,
        Location origin,
        Location destination,
        double distanceKm,
        int estimatedMinutes,
        double cost,
        double carbonEmission,
        boolean available,
        int trafficLevel,
        int weatherRisk
) {
    public Route {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException(
                    "Route ID cannot be empty"
            );
        }

        if (origin == null || destination == null) {
            throw new IllegalArgumentException(
                    "Origin and destination are required"
            );
        }

        if (!Double.isFinite(distanceKm) || distanceKm < 0) {
            throw new IllegalArgumentException(
                    "Distance cannot be negative or invalid"
            );
        }

        if (estimatedMinutes <= 0) {
            throw new IllegalArgumentException(
                    "Delivery time must be positive"
            );
        }

        if (!Double.isFinite(cost) || cost < 0) {
            throw new IllegalArgumentException(
                    "Cost cannot be negative or invalid"
            );
        }

        if (!Double.isFinite(carbonEmission) || carbonEmission < 0) {
            throw new IllegalArgumentException(
                    "Carbon emission cannot be negative or invalid"
            );
        }

        if (trafficLevel < 0 || trafficLevel > 10) {
            throw new IllegalArgumentException(
                    "Traffic level must be between 0 and 10"
            );
        }

        if (weatherRisk < 0 || weatherRisk > 10) {
            throw new IllegalArgumentException(
                    "Weather risk must be between 0 and 10"
            );
        }
    }
}
