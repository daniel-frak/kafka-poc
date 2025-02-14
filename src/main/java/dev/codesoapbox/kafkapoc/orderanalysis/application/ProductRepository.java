package dev.codesoapbox.kafkapoc.orderanalysis.application;

import dev.codesoapbox.kafkapoc.ordering.application.domain.Product;

import java.math.BigDecimal;
import java.util.List;

public class ProductRepository {

    private final List<Product> products = List.of(
            new Product(1L, "Product 1", BigDecimal.valueOf(12.50)),
            new Product(2L, "Product 2", BigDecimal.valueOf(50)),
            new Product(3L, "Product 3", BigDecimal.valueOf(100))
    );

    public Product getById(long id) {
        return products.stream()
                .filter(p -> p.id() == id)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Product with id " + id + " not found"));
    }
}
