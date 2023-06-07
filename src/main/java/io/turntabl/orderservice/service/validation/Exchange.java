package io.turntabl.orderservice.service.validation;

import io.turntabl.orderservice.config.ExchangeConfig;
import io.turntabl.orderservice.models.ProductData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;

@Component
public class Exchange {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ExchangeConfig exchangeConfig;

    private ProductData getProductData(String url) {
        URI uri = null;
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        ProductData prodData = restTemplate.getForObject(uri, ProductData.class);
        return prodData;
    }

    ProductData getProductDataFromExchange1(String ticker) {
        String exchangeOneUrl = getUrlForExchangeMDS("1", ticker);
        return getProductData(exchangeOneUrl);
    }

    ProductData getProductDataFromExchange2(String ticker) {
        String exchangeTwoUrl = getUrlForExchangeMDS("2", ticker);
        return getProductData(exchangeTwoUrl);
    }

    private String getUrlForExchangeMDS(String exNum, String ticker) {
        String exUrl = "http://localhost:9091/api/v1/product/pd/";
        StringBuilder builder = new StringBuilder(exUrl);
        builder.append(exNum).append("/").append(ticker);
        return builder.toString();
    }

}
