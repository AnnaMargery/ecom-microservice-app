package com.app.order.repository;

import com.app.order.model.CartItem;
import com.app.order.model.Order;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
	@Repository
	interface CartItemRepository extends JpaRepository<CartItem, Long> {

		CartItem findByUserAndProduct(String userId, String productId);

		void deleteByUserAndProduct(String userId, String productId);

		List<CartItem> findByUser(String userId);

		void deleteByUser(String userId);
	}
}
