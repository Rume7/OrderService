package io.turntabl.orderservice.queueingsys.service;

import io.turntabl.orderservice.models.Order;

public interface OrderMQService {

    void sendMallonRequest(Order orderData);
}
