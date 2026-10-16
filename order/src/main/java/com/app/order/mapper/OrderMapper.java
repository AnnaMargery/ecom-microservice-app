package com.app.order.mapper;

import com.app.order.dto.OrderItemDto;
import com.app.order.dto.OrderResponse;
import com.app.order.model.Order;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class OrderMapper {

	public OrderResponse mapToOrderResponse(Order order) {
		return new OrderResponse(order.getId(), order.getAmount(), order.getStatus(),
				order.getItems().stream()
						.map(item -> new OrderItemDto(item.getId(), item.getProductId(), item.getQuantity(),
								item.getPrice(), item.getPrice().multiply(new BigDecimal(item.getQuantity()))))
						.toList(),
				order.getCreatedAt());
	}

}
