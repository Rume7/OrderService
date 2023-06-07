package io.turntabl.orderservice.queueingsys.config;

import io.turntabl.orderservice.models.Order;
import io.turntabl.orderservice.models.Side;
import io.turntabl.orderservice.models.Type;
import io.turntabl.orderservice.queueingsys.service.OrderMQService;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

// This component just puts data into rabbit MQ for test
@Component
public class InitRabbit {

    private final OrderMQService rabbitMQService;

    public InitRabbit(OrderMQService rabbitMQService) {
        this.rabbitMQService = rabbitMQService;
    }

    @Bean
    public RestTemplate getRestTemplate() {
        return new RestTemplate();
    }

    public void run(String... args) throws Exception {
        testData().forEach(data -> {
            rabbitMQService.sendMallonRequest(data);
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public List<Order> testData(){
        var data1 = new Order("IBM", 23, 1.5, Side.BUY, Type.MARKET);
        var data2 = new Order("AMZN", 23, 1.5, Side.SELL, Type.LIMIT);
        var data3 = new Order("NFLX", 30, 1.5, Side.BUY, Type.MARKET);
        var data4 = new Order("MSFT", 35, 1.5, Side.BUY, Type.MARKET);

        return List.of(data1, data2,data3, data4, data1);
    }
}
