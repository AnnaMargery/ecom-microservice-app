package com.app.order.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CartItemRequest {

	@NotNull(message = "Product ID is required")
	private String productId;
	@NotNull(message = "Quantity is required")
	@Positive(message = "Quantity must be greater than 0")
	private Integer quantity;
}
