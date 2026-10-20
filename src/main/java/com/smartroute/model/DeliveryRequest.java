package com.smartroute.model;

public record DeliveryRequest(
        DeliveryPriority priority,
        double maxBudget,
        int maxDeliveryMinutes,
        boolean ecoPreferred
) {
    public DeliveryRequest {
        if (priority == null) {
            throw new IllegalArgumentException(
                    "Delivery priority is required"
            );
        }

        if (!Double.isFinite(maxBudget) || maxBudget < 0) {
            throw new IllegalArgumentException(
                    "Budget cannot be negative or invalid"
            );
        }

        if (maxDeliveryMinutes <= 0) {
            throw new IllegalArgumentException(
                    "Delivery deadline must be positive"
            );
        }
    }
}
