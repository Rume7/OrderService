package io.turntabl.orderservice.controller;

import io.turntabl.orderservice.models.Order;
import io.turntabl.orderservice.queueingsys.service.OrderMQService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OrderQueueProcessor {

    @Autowired
    private OrderMQService orderMQService;

    public void sendOrderToMessageQueue(Order order) {
        orderMQService.sendMallonRequest(order);
    }
}
