package io.turntabl.orderservice.queueingsys.service;

import io.turntabl.orderservice.models.Order;
import io.turntabl.orderservice.queueingsys.config.OrderConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class OrderMQServiceImpl implements OrderMQService {

    private final RabbitTemplate rabbitTemplate;

    public OrderMQServiceImpl(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void sendMallonRequest(Order orderData) {
        Order validOrder = (Order) rabbitTemplate.convertSendAndReceive(OrderConfig.exchangeName, OrderConfig.routingKey, orderData);
    }
}
