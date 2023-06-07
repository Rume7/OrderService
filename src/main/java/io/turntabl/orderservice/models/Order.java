package io.turntabl.orderservice.models;


import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Data
@JsonSerialize
@Entity(name="Client_Order")
public class Order implements Serializable {

    @Id
    private UUID id;

    private String orderId;
    private String product;
    private long quantity;
    private double price;

    @Enumerated(EnumType.STRING)
    private Side side;

    @Enumerated(EnumType.STRING)
    private Type type;
    private Instant dateTimeCreated;

    public Order() {
        this.id = UUID.randomUUID();
        this.dateTimeCreated = Instant.now();
    }

    public Order(String product, long quantity, double price, Side side, Type type) {
        this.id = UUID.randomUUID();
        this.product = product;
        this.quantity = quantity;
        this.price = price;
        this.side = side;
        this.orderId = "";
        this.type = Type.valueOf(type.name());
        this.dateTimeCreated = Instant.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return id.equals(order.id) && product.equals(order.product);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, product);
    }
}
