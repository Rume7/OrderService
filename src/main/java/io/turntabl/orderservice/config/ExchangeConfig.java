package io.turntabl.orderservice.config;

import io.turntabl.orderservice.service.validation.Exchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("exchange")
public class ExchangeConfig {

    @Value("${exchange.url1}")
    private String exchange1Url;

    @Value("${exchange.url2}")
    private String exchange2Url;

    @Value("${exchange.apiKey}")
    private String apiKey;

    public String getExchange1Url() {
        return exchange1Url;
    }

    public String getExchange2Url() {
        return exchange2Url;
    }

    public String getApiKey() {
        return apiKey;
    }

    @Bean
    public Exchange getExchange() {
        return new Exchange();
    }

    @Override
    public String toString() {
        return "ExchangeConfig{exchange1='" + exchange1Url + ", exchange2='" + exchange2Url + '}';
    }
}
