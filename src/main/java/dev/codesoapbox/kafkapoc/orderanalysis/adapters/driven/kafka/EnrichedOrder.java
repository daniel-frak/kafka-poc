package dev.codesoapbox.kafkapoc.orderanalysis.adapters.driven.kafka;

import dev.codesoapbox.kafkapoc.ordering.application.domain.Product;

import java.util.List;

public record EnrichedOrder(
        String orderId,
        List<Product> products
) {
}
