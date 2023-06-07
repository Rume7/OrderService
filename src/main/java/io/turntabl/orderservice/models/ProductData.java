package io.turntabl.orderservice.models;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize
public record ProductData(double BID_PRICE, long SELL_LIMIT, double ASK_PRICE,
                          long BUY_LIMIT, double MAX_PRICE_SHIFT, String TICKER, double LAST_TRADED_PRICE) {

}
