package dev.codesoapbox.kafkapoc.ordering.application.domain;

import java.math.BigDecimal;

public record Product(
        long id,
        String name,
        BigDecimal price
) {
}
