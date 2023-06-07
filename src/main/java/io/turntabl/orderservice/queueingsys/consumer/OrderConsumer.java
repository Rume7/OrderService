package io.turntabl.orderservice.queueingsys.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.turntabl.orderservice.models.Order;
import io.turntabl.orderservice.queueingsys.config.OrderConfig;
import io.turntabl.orderservice.service.orderprocessing.OrderProcessingService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class OrderConsumer {

    @Autowired
    private OrderProcessingService orderProcessingService;

    @RabbitListener(queues = OrderConfig.queueName)
    public void consumerOrder(Order freshOrder) throws JsonProcessingException {
        orderProcessingService.sendOrderToExchange(freshOrder);
    }
}
