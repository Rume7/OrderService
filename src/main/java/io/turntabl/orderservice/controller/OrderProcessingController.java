package io.turntabl.orderservice.controller;

import io.turntabl.orderservice.models.Order;
import io.turntabl.orderservice.pubSub.RedisConfig;
import io.turntabl.orderservice.pubSub.RedisPublisher;
import io.turntabl.orderservice.service.orderprocessing.OrderProcessingService;
import io.turntabl.orderservice.service.validation.OrderValidatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@CrossOrigin(origins = "http://localhost:4200/")
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
    @CrossOrigin(originPatterns = "*")
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


    @GetMapping(value="/all", produces = MediaType.APPLICATION_JSON_VALUE)
    @CrossOrigin(originPatterns = "*")
    public ResponseEntity<?> getAllOrders(){
        List<Order> allOrders = orderProcessingService.getAllOrders();
        return ResponseEntity.status(HttpStatus.FOUND).body(allOrders);
    }

    @CrossOrigin(originPatterns = "*")
    @DeleteMapping("/cancel/{orderId}")
    public ResponseEntity<?> cancelAnOrder(@PathVariable("orderId") String id) {
        orderValidatorService.cancelOrder(id);
        return ResponseEntity.status(HttpStatus.valueOf("Order Cancelled")).body(id);
    }
}
