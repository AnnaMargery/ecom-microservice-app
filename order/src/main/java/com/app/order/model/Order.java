package com.app.order.model;

import static com.app.order.model.OrderStatus.*;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Data
@NoArgsConstructor
@Table(name = "orders")
public class Order {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String userId;
	private BigDecimal amount;
	@Enumerated(EnumType.STRING)
	private OrderStatus status = PENDING;
	@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<OrderItem> items = new ArrayList<>();
	@CreationTimestamp
	private LocalDateTime createdAt;
	@UpdateTimestamp
	private LocalDateTime updatedAt;

	@Entity
	@Data
	@NoArgsConstructor
	@Table(name = "cartItems")
	public static class CartItem {

		@Id
		@GeneratedValue(strategy = GenerationType.IDENTITY)
		private Long id;
		private String userId;
		private String productId;
		private Integer quantity;
		private BigDecimal price;
		@CreationTimestamp
		private LocalDateTime createdAt;
		@UpdateTimestamp
		private LocalDateTime updatedAt;

	}
}
