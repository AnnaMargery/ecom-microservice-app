package com.ecom.app.product.mapper;

import com.ecom.app.product.dto.ProductRequest;
import com.ecom.app.product.dto.ProductResponse;
import com.ecom.app.product.model.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

	public ProductResponse mapToProductResponse(Product product) {
		return ProductResponse.builder()
				.id(product.getId())
				.name(product.getName())
				.price(product.getPrice())
				.description(product.getDescription())
				.imageUrl(product.getImageUrl())
				.category(product.getCategory())
				.stockQuantity(product.getStockQuantity())
				.isActive(product.isActive())
				.build();
	}

	public void updateProductFromRequest(Product product, ProductRequest productRequest) {
		product.setName(productRequest.getName());
		product.setDescription(productRequest.getDescription());
		product.setPrice(productRequest.getPrice());
		product.setStockQuantity(productRequest.getStockQuantity());
		product.setCategory(productRequest.getCategory());
		product.setImageUrl(productRequest.getImageUrl());
	}
}
