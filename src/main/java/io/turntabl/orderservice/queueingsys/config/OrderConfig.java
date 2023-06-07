package io.turntabl.orderservice.queueingsys.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OrderConfig {

    public static final String exchangeName = "r2mExchange";
    public static final String queueName = "r2mQueue";
    public static final String routingKey = "r2mKey";

    @Bean
    DirectExchange directExchange() {
        return new DirectExchange(exchangeName, true, false);
    }

    @Bean
    Queue r2mQueue() {
        return new Queue(queueName, true);
    }

    @Bean
    Binding binding(Queue r2mQueue, DirectExchange exchange) {
        return BindingBuilder.bind(r2mQueue).to(exchange).with(routingKey);
    }
}
