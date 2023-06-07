package io.turntabl.orderservice.controller;

import io.turntabl.orderservice.models.Order;
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

    @PostMapping("/order")
    public ResponseEntity<?> placeAnOrder(@RequestBody Order order) {
        boolean orderStatus = orderValidatorService.validateOrder(order);
        if (orderStatus) {
            orderQueueProcessor.sendOrderToMessageQueue(order);
            UUID id = order.getId();
            Order retrievedOrder = orderProcessingService.getAnOrder(id);
            return ResponseEntity.status(HttpStatus.CREATED).body(retrievedOrder);
        } else {
            // Handle failed processed order => throw exception by sending message to client.
//          // Send order to the redis under the topic of invalid orders.

            return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).body(order);
        }
    }

    @DeleteMapping("/cancel/{orderId}")
    public ResponseEntity<?> cancelAnOrder(@PathVariable("orderId") String id) {
        orderValidatorService.cancelOrder(id);
        return ResponseEntity.status(HttpStatus.valueOf("Order Cancelled")).body(id);
    }
}
