package dev.codesoapbox.kafkapoc.ordering.application.domain;

import lombok.NonNull;

import java.util.List;

public record OrderCreatedEvent(
        @NonNull String orderId,
        @NonNull List<Long> productIds
) {
}
