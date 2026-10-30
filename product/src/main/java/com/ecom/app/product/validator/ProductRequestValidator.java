package com.ecom.app.product.validator;

import com.ecom.app.product.dto.ProductRequest;
import org.springframework.stereotype.Component;

@Component
public class ProductRequestValidator {

    	public void validateProductRequest(ProductRequest request) {
		if (request.getName() == null || request.getName().isBlank()) {
			throw new IllegalArgumentException("Product name is required");
		}
		if (request.getCategory() == null || request.getCategory().isBlank()) {
			throw new IllegalArgumentException("Category is required");
		}
		if (request.getPrice() == null || request.getPrice().compareTo(java.math.BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException("Price must be greater than 0");
		}
		if (request.getStockQuantity() != null && request.getStockQuantity() < 0) {
			throw new IllegalArgumentException("Stock quantity cannot be negative");
		}
	}
}
