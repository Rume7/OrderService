package io.turntabl.orderservice.service.validation;

import io.turntabl.orderservice.models.Order;

public interface ExchangeService {

    long getBuyQuantityOnExchange1(Order order);

    long getBuyQuantityOnExchange2(Order order);

    long getSellQuantityOnExchange1(Order order);

    long getSellQuantityOnExchange2(Order order);
}