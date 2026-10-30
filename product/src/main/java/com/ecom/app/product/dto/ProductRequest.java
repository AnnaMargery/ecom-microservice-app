package com.ecom.app.product.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductRequest {

	@NotBlank(message = "Product name is required")
	private String name;
	private String description;
	@NotNull(message = "Price is required")
	@Positive(message = "Price must be greater than 0")
	private BigDecimal price;
	@PositiveOrZero(message = "Stock quantity cannot be negative")
	private Integer stockQuantity;
	@NotBlank(message = "Category is required")
	private String category;
	private String imageUrl;
}
