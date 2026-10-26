package com.app.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@Builder
public class OrderItemDto {

	private Long id;
	private String productId;
	private Integer quantity;
	private BigDecimal price;
	private BigDecimal subTotal;

	@Data
	public static class CartItemRequest {

		private Long productId;
		private Integer quantity;
	}
}
