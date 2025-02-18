package dev.codesoapbox.kafkapoc.orderanalysis.adapters.driving.http;

import dev.codesoapbox.kafkapoc.orderanalysis.adapters.driven.kafka.HourlyProductSalesInfoKTable;
import dev.codesoapbox.kafkapoc.orderanalysis.application.ProductSalesInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("ordered-products")
@RequiredArgsConstructor
public class OrderedProductInfoController {

    private final HourlyProductSalesInfoKTable hourlyProductSalesInfoKTable;

    @GetMapping("{productId}")
    public Optional<ProductSalesInfo> getProductSalesInfo(@PathVariable long productId) {
        return hourlyProductSalesInfoKTable.getProductSalesInfo(productId);
    }
}
