package io.turntabl.orderservice.controller;

import io.turntabl.orderservice.models.Order;
import io.turntabl.orderservice.pubSub.RedisConfig;
import io.turntabl.orderservice.pubSub.RedisPublisher;
import io.turntabl.orderservice.service.orderprocessing.OrderProcessingService;
import io.turntabl.orderservice.service.validation.OrderValidatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("api/v1/orders")
public class OrderProcessingController {

    @Autowired
    private OrderValidatorService orderValidatorService;

    @Autowired
    private OrderQueueProcessor orderQueueProcessor;

    @Autowired
    private OrderProcessingService orderProcessingService;

    @Autowired
    private RedisPublisher redisPublisher;

    @PostMapping("/order")
    public ResponseEntity<?> placeAnOrder(@RequestBody Order order) {
        boolean orderStatus = orderValidatorService.validateOrder(order);
        if (orderStatus) {
            orderQueueProcessor.sendOrderToMessageQueue(order);
            UUID id = order.getId();
            Order retrievedOrder = orderProcessingService.getAnOrder(id);
            return ResponseEntity.status(HttpStatus.CREATED).body(retrievedOrder);
        } else {
           // publish invalid orders to redis.
            Object orderObject = order;
            redisPublisher.publishMessage(orderObject.toString());
            return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).body(order);
        }
    }

    @DeleteMapping("/cancel/{orderId}")
    public ResponseEntity<?> cancelAnOrder(@PathVariable("orderId") String id) {
        orderValidatorService.cancelOrder(id);
        return ResponseEntity.status(HttpStatus.valueOf("Order Cancelled")).body(id);
    }
}
