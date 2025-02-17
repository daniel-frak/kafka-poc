package dev.codesoapbox.kafkapoc.orderanalysis.application;

import dev.codesoapbox.kafkapoc.ordering.application.domain.Product;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@EqualsAndHashCode
public class ProductSalesInfo {

    private long productId;
    private BigDecimal latestPrice;
    private BigDecimal totalPricePaid = BigDecimal.ZERO;
    private long quantity = 0;

    // @TODO This is currently leaking Kafka architecture into application
    public ProductSalesInfo process(Long productId, Product product) {
        this.productId = productId;
        this.latestPrice = product.price();
        this.totalPricePaid = totalPricePaid.add(product.price());
        this.quantity += 1;

        return this;
    }
}
