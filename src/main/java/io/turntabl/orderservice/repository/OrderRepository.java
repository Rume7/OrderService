package io.turntabl.orderservice.repository;

import io.turntabl.orderservice.models.Order;
import io.turntabl.orderservice.models.OrderDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface OrderRepository  extends JpaRepository<Order, UUID> {

}
