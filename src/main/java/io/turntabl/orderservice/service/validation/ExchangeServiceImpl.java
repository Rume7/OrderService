package io.turntabl.orderservice.service.validation;

import io.turntabl.orderservice.models.Order;
import io.turntabl.orderservice.models.ProductData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ExchangeServiceImpl implements ExchangeService {

    @Autowired
    private Exchange exchange;

    @Override
    public long getBuyQuantityOnExchange1(Order order) {
        ProductData productData = exchange.getProductDataFromExchange1(order.getProduct());
        return productData.BUY_LIMIT();
    }

    @Override
    public long getSellQuantityOnExchange1(Order order) {
        ProductData productData = exchange.getProductDataFromExchange1(order.getProduct());
        return productData.SELL_LIMIT();
    }

    @Override
    public long getBuyQuantityOnExchange2(Order order) {
        ProductData productData = exchange.getProductDataFromExchange2(order.getProduct());
        return productData.BUY_LIMIT();
    }

    @Override
    public long getSellQuantityOnExchange2(Order order) {
        ProductData productData = exchange.getProductDataFromExchange2(order.getProduct());
        return productData.SELL_LIMIT();
    }
}
