package com.yourname.urlshortener.repository;

import com.yourname.urlshortener.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
