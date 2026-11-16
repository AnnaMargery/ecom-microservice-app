package com.ecom.app.product.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.ecom.app.product.dto.ProductResponse;
import com.ecom.app.product.model.Product;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
@SpringBootTest
class ProductMapperTest {

	@Autowired
	private ProductMapper productMapper;

	@Test
	void mapToProductResponse() {
		// Given
		Product product = mockProduct();
		// When
		ProductResponse productResponse = productMapper.mapToProductResponse(product);
		// Then
		assertThat(productResponse).usingRecursiveComparison().isEqualTo(product);
	}

	private Product mockProduct() {
		return Product.builder().id(2222222L).name("Product name").description("Product Description")
				.category("Product category").imageUrl("hhtp://productimage.com").price(BigDecimal.valueOf(11111L))
				.isActive(true).stockQuantity(2).build();
	}

}