package io.turntabl.orderservice.service.orderprocessing;

import io.turntabl.orderservice.config.ExchangeConfig;
import io.turntabl.orderservice.models.Order;
import io.turntabl.orderservice.models.OrderDTO;
import io.turntabl.orderservice.models.Side;
import io.turntabl.orderservice.pubSub.RedisPublisher;
import io.turntabl.orderservice.repository.OrderRepository;
import io.turntabl.orderservice.service.validation.ExchangeExecution;
import io.turntabl.orderservice.service.validation.ExchangeService;
import io.turntabl.orderservice.service.validation.OrderValidatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class OrderProcessingService {

    @Autowired
    private OrderValidatorService orderValidatorService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ExchangeConfig exchangeConfig;

    @Autowired
    private ExchangeService exchangeService;

    private final RedisPublisher redisPublisher;
    @Autowired
    public OrderProcessingService(OrderRepository orderRepository, RestTemplate restTemplate , RedisPublisher redisPublisher) {
        this.orderRepository = orderRepository;
        this.restTemplate = restTemplate;
        this.redisPublisher = redisPublisher;
    }

    public Object sendOrderToExchange(Order order) {
        //ResponseEntity<Object> response = executeOnExchange(order);
        ResponseEntity<?> response = processAndExecuteOnExchange(order);

        //publish gotten order to redis
        redisPublisher.publishMessage(((Object)response.getBody()));
        return response;
    }

    private ExchangeExecution chooseExchangeToExecute(Order order) {
        if (orderValidatorService.isPriceSuitableFromExchange1(order)) {
            if (orderValidatorService.isQuantityAvailableOnExchange1(order)) {
                return ExchangeExecution.ONE;
            }
            return ExchangeExecution.SPLIT;
        }
        if (orderValidatorService.isPriceSuitableFromExchange2(order)) {
            if (orderValidatorService.isQuantityAvailableOnExchange2(order)) {
                return ExchangeExecution.TWO;
            }
            return ExchangeExecution.SPLIT;
        }
        return ExchangeExecution.NO_EXCHANGE;
    }

    public ResponseEntity<?> processAndExecuteOnExchange(Order order) {
        String urlLink1 = buildOrderURL(exchangeConfig.getExchange1Url());
        String urlLink2 = buildOrderURL(exchangeConfig.getExchange2Url());

        if (chooseExchangeToExecute(order) == ExchangeExecution.ONE) {
            ResponseEntity<Order> response = executeOnExchange(urlLink1, order);
            order.setOrderId(response.getBody().getOrderId());
            Order gottenOrder = orderRepository.save(order);
            return response;
        } else if (chooseExchangeToExecute(order) == ExchangeExecution.TWO) {
            ResponseEntity<Order> response = executeOnExchange(urlLink2, order);
            order.setOrderId(response.getBody().getOrderId());
            Order gottenOrder = orderRepository.save(order);
            return response;
        } else if (chooseExchangeToExecute(order) == ExchangeExecution.SPLIT) {
            return multiLegExecution(List.of(urlLink1, urlLink2), order);
        }
        return ResponseEntity.badRequest().body(null);
    }

    private ResponseEntity<Object> multiLegExecution(List listOfUrlLinks, Order order) {
        long qty1 = 0, qty2 = 0;
        if (order.getSide() == Side.BUY) {
            qty1 = exchangeService.getBuyQuantityOnExchange1(order);
            qty2 = exchangeService.getBuyQuantityOnExchange2(order);
        } else if (order.getSide() == Side.SELL) {
            qty1 = exchangeService.getSellQuantityOnExchange1(order);
            qty2 = exchangeService.getSellQuantityOnExchange2(order);
        }
        Order order1 = new Order(order.getProduct(), qty1, order.getPrice(), order.getSide(), order.getType());
        Order order2 = new Order(order.getProduct(), order.getQuantity()-qty1, order.getPrice(), order.getSide(), order.getType());
        ResponseEntity<Order> orderPlaced1 = executeOnExchange(listOfUrlLinks.get(0).toString(), order1);
        ResponseEntity<Order> orderPlaced2 = executeOnExchange(listOfUrlLinks.get(1).toString(), order2);
        Order newOrder1 = orderRepository.save(orderPlaced1.getBody());
        Order newOrder2 = orderRepository.save(orderPlaced2.getBody());
        return ResponseEntity.status(HttpStatus.CREATED).body(List.of(newOrder1, newOrder2));
    }

    private ResponseEntity<Order> executeOnExchange(String urlLink, Order order) {
        OrderDTO orderDTO = new OrderDTO(order.getProduct(), order.getQuantity(), order.getPrice(),
                order.getSide(), order.getType());
        ResponseEntity<Order> object = restTemplate.postForEntity(urlLink, orderDTO, Order.class);
        return object;
    }

    private ResponseEntity<Object> executeOnExchange(Order order) {
        String urlLink = buildOrderURL(exchangeConfig.getExchange1Url());
        OrderDTO orderDTO = new OrderDTO(order.getProduct(), order.getQuantity(), order.getPrice(),
                order.getSide(), order.getType());
        ResponseEntity<Object> object = restTemplate.postForEntity(urlLink, orderDTO, Object.class);
        return object;
    }

    public Order getAnOrder(UUID id) {
        Optional<Order> anOrder = orderRepository.findById(id);
        return anOrder.orElse(null);
    }

    private String buildOrderURL(String exchangeUrlUsed) {
        StringBuilder builder = new StringBuilder(exchangeUrlUsed);
        builder.append("/").append(exchangeConfig.getApiKey()).append("/").append("order");
        return builder.toString();
    }
}
