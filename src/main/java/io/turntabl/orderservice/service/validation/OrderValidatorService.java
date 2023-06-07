package io.turntabl.orderservice.service.validation;

import io.turntabl.orderservice.models.Order;
import io.turntabl.orderservice.models.ProductData;
import io.turntabl.orderservice.models.Side;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderValidatorService {

    @Autowired
    private Exchange exchange;

    @Autowired
    private ExchangeService exchangeService;

    public boolean validateOrder(Order order) {
        return firstOrderValidation(order);
        //return validateOrderForExchange1(order) || validateOrderForExchange2(order);
    }

    private boolean firstOrderValidation(Order order) {
        return (isPriceSuitableFromExchange1(order) || isPriceSuitableFromExchange1(order)) &&
                canExchangesHandleBothQuantities(order);
    }

    private boolean validateOrderForExchange1(Order order) {
        return isPriceSuitableFromExchange1(order) && isQuantityAvailableOnExchange1(order);
    }

    private boolean validateOrderForExchange2(Order order) {
        return isPriceSuitableFromExchange2(order) && isQuantityAvailableOnExchange2(order);
    }

    public boolean isPriceSuitableFromExchange1(Order order) {
        if (order == null) return false;
        double clientPrice = order.getPrice();
        ProductData productData = exchange.getProductDataFromExchange1(order.getProduct());

        if (order.getSide() == Side.BUY) {
            return (clientPrice >= productData.ASK_PRICE()) &&
                    (clientPrice <= productData.ASK_PRICE() + productData.MAX_PRICE_SHIFT());
        } else if (order.getSide() == Side.SELL) {
            return (clientPrice <= productData.BID_PRICE()) &&
                    (clientPrice >= productData.ASK_PRICE() + productData.MAX_PRICE_SHIFT());
        }
        return false;
    }

    public boolean isPriceSuitableFromExchange2(Order order) {
        if (order == null) return false;
        double clientPrice = order.getPrice();
        ProductData productData = exchange.getProductDataFromExchange2(order.getProduct());

        if (order.getSide() == Side.BUY) {
            return (clientPrice >= productData.ASK_PRICE()) &&
                    (clientPrice <= productData.ASK_PRICE() + productData.MAX_PRICE_SHIFT());
        } else if (order.getSide() == Side.SELL) {
            return (clientPrice <= productData.BID_PRICE()) &&
                    (clientPrice >= productData.ASK_PRICE() + productData.MAX_PRICE_SHIFT());
        }
        return false;
    }

    public boolean isQuantityAvailableOnExchange1(Order order) {
        if (order == null) return false;

        long clientQty = order.getQuantity();
        ProductData productData = exchange.getProductDataFromExchange1(order.getProduct());

        if (order.getSide() == Side.BUY) {
            return clientQty <= productData.SELL_LIMIT();
        } else if (order.getSide() == Side.SELL) {
            return clientQty <= productData.BUY_LIMIT();
        }
        return false;
    }

    public boolean isQuantityAvailableOnExchange2(Order order) {
        if (order == null) return false;

        long clientQty = order.getQuantity();
        ProductData productData = exchange.getProductDataFromExchange2(order.getProduct());
        if (order.getSide() == Side.BUY) {
            return clientQty <= productData.SELL_LIMIT();
        } else if (order.getSide() == Side.SELL) {
            return clientQty <= productData.BUY_LIMIT();
        }
        return false;
    }

    private boolean canExchangesHandleBothQuantities(Order order) {
        ProductData productData1 = exchange.getProductDataFromExchange1(order.getProduct());
        ProductData productData2 = exchange.getProductDataFromExchange2(order.getProduct());

        if (order.getSide() == Side.BUY) {
            return order.getQuantity() <= (productData1.SELL_LIMIT() + productData2.SELL_LIMIT());
        } else if (order.getSide() == Side.SELL) {
            return order.getQuantity() <= (productData1.BUY_LIMIT() + productData2.BUY_LIMIT());
        }
        return false;
    }

    public void cancelOrder(String orderId) {
        // check status of the order. if not executed, and order is a LIMIT, delete.

        // If order is a MARKET, close the order.
        // If executed, inform client and cancel at a loss
    }
}
