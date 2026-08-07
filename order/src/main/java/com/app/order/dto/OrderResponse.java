package com.app.order.dto;

import com.app.order.model.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OrderResponse {

	private Long id;
	private BigDecimal totalAmount;
	private OrderStatus status;
	private List<OrderItemDto> items;
	private LocalDateTime createdAt;

}
