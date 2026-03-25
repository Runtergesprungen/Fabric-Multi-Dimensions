package net.runter.multidimensions.dimensions;

public record CreationQueueResult(
        int totalTargets,
        int validTargets,
        int invalidTargets,
        String summary
) {
}