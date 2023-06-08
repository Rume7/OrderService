package io.turntabl.orderservice.service.orderprocessing;

import io.turntabl.orderservice.config.ExchangeConfig;
import io.turntabl.orderservice.models.Order;
import io.turntabl.orderservice.models.OrderDTO;
import io.turntabl.orderservice.models.Type;
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

    @Autowired
    public OrderProcessingService(OrderRepository orderRepository, RestTemplate restTemplate) {
        this.orderRepository = orderRepository;
        this.restTemplate = restTemplate;
    }

    public Order sendOrderToExchange(Order order) {

        //ResponseEntity<Object> response = executeOnExchange(order);
        ResponseEntity<Object> response = processAndExecuteOnExchange(order);


        order.setOrderId(response.getBody().toString());
        Order gottenOrder = orderRepository.save(order);
        System.out.println(gottenOrder);
        return gottenOrder;
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

    private ResponseEntity<Object> processAndExecuteOnExchange(Order order) {
        String urlLink1 = buildOrderURL(exchangeConfig.getExchange1Url());
        String urlLink2 = buildOrderURL(exchangeConfig.getExchange2Url());

        if (chooseExchangeToExecute(order) == ExchangeExecution.ONE) {
            return executeOnExchange(urlLink1, order);
        } else if (chooseExchangeToExecute(order) == ExchangeExecution.TWO) {
            return executeOnExchange(urlLink2, order);
        } else if (chooseExchangeToExecute(order) == ExchangeExecution.SPLIT) {
            return multiLegExecution(List.of(urlLink1, urlLink2), order);
        }
        return ResponseEntity.badRequest().body(null);
    }

    private ResponseEntity<Object> multiLegExecution(List listOfUrlLinks, Order order) {
        long qty1 = exchangeService.getBuyQuantityOnExchange1(order);
        long qty2 = exchangeService.getBuyQuantityOnExchange2(order);

        Order order1 = new Order(order.getProduct(), qty1, order.getPrice(), order.getSide(), order.getType());
        Order order2 = new Order(order.getProduct(), order.getQuantity()-qty1, order.getPrice(), order.getSide(), order.getType());
        ResponseEntity<Object> object1 = executeOnExchange(listOfUrlLinks.get(0).toString(), order1);
        ResponseEntity<Object> object2 = executeOnExchange(listOfUrlLinks.get(1).toString(), order2);
        return ResponseEntity.status(HttpStatus.CREATED).body(List.of(object1, object2));
    }

    private ResponseEntity<Object> executeOnExchange(String urlLink, Order order) {
        OrderDTO orderDTO = new OrderDTO(order.getProduct(), order.getQuantity(), order.getPrice(),
                order.getSide(), order.getType());
        ResponseEntity<Object> object = restTemplate.postForEntity(urlLink, orderDTO, Object.class);
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

    public List<Order> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        return orders;
    }

    private String buildOrderURL(String exchangeUrlUsed) {
        StringBuilder builder = new StringBuilder(exchangeUrlUsed);
        builder.append("/").append(exchangeConfig.getApiKey()).append("/").append("order");
        return builder.toString();
    }
}
