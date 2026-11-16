package com.ecom.app.product.controller;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ecom.app.product.dto.ProductRequest;
import com.ecom.app.product.dto.ProductResponse;
import com.ecom.app.product.service.ProductService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

	@Autowired
	MockMvc mockMvc;

	@MockitoBean
	ProductService productService;

	@Autowired
	ObjectMapper objectMapper;

	@Test
	void shouldReturnProductWhenFound() throws Exception {
		ProductResponse product = mockProductResponse();

		given(productService.getProductById(1L)).willReturn(Optional.of(product));

		mockMvc.perform(get("/api/products/1")).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.name").value("Keyboard"));
	}

	@Test
	void shouldReturn404WhenProductIsMissing() throws Exception {
		given(productService.getProductById(999L)).willReturn(Optional.empty());

		mockMvc.perform(get("/api/products/999")).andExpect(status().isNotFound());
	}

	@Test
	void shouldCreateValidProduct() throws Exception {
		ProductRequest productRequest = mockProductRequest();
		ProductResponse productResponse = mockProductResponse();

		given(productService.createProduct(productRequest)).willReturn(productResponse);

		mockMvc.perform(post("/api/products").contentType("application/json")
				.content(objectMapper.writeValueAsString(productRequest))).andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(productResponse.getId()))
				.andExpect(jsonPath("$.name").value(productResponse.getName()));
	}

	@Test
	void shouldNotCreateInvalidProduct() throws Exception {
		ProductRequest invalidRequest = ProductRequest.builder().name("").price(new BigDecimal("-1.00"))
				.stockQuantity(-1).category("").build();

		mockMvc.perform(post("/api/products").contentType("application/json")
				.content(objectMapper.writeValueAsString(invalidRequest))).andExpect(status().isBadRequest());

		verifyNoInteractions(productService);
	}

	@Test
	void shouldUpdateProduct() throws Exception {
		ProductRequest productRequest = mockProductRequest();
		ProductResponse productResponse = mockProductResponse();

		given(productService.updateProduct(1L, productRequest)).willReturn(Optional.ofNullable(productResponse));

		mockMvc.perform(put("/api/products/1").contentType("application/json")
				.content(objectMapper.writeValueAsString(productRequest))).andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(productResponse.getId()))
				.andExpect(jsonPath("$.name").value(productResponse.getName()));
	}

	@Test
	void shouldGetAllProducts() throws Exception {
		List<ProductResponse> productResponses = mockSampleProducts();
		given(productService.getAllProducts()).willReturn(productResponses);

		mockMvc.perform(get("/api/products")).andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(productResponses.size()))
				.andExpect(jsonPath("$[0].id").value(productResponses.get(0).getId()))
				.andExpect(jsonPath("$[0].name").value(productResponses.get(0).getName()))
				.andExpect(jsonPath("$[1].id").value(productResponses.get(1).getId()))
				.andExpect(jsonPath("$[1].name").value(productResponses.get(1).getName()));
	}

	@Test
	void shouldGetEmptyListWhenAllProductsAreMissing() throws Exception {
		List<ProductResponse> productResponses = List.of();
		given(productService.getAllProducts()).willReturn(productResponses);

		mockMvc.perform(get("/api/products")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
	}

	@Test
	void shouldDeleteProduct() throws Exception {
		given(productService.deleteProduct(1L)).willReturn(true);

		mockMvc.perform(delete("/api/products/1")).andExpect(status().isNoContent());
	}

	@Test
	void shouldSearchProducts() throws Exception {
		ProductResponse productResponse = mockProductResponse();
		given(productService.searchByKeyword("Product")).willReturn(List.of(productResponse));

		mockMvc.perform(get("/api/products/search").param("keyword", "Product")).andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1));
	}

	private ProductRequest mockProductRequest() {
		return ProductRequest.builder().name("Keyboard").description("Mechanical keyboard")
				.price(new BigDecimal("49.99")).stockQuantity(100).category("Accessories")
				.imageUrl("http://example.com/keyboard.jpg").build();
	}

	private ProductResponse mockProductResponse() {
		return ProductResponse.builder().id(1L).name("Keyboard").price(new BigDecimal("49.99")).category("Accessories")
				.build();
	}

	private List<ProductResponse> mockSampleProducts() {
		ProductResponse product1 = ProductResponse.builder().id(1L).name("Product 1").description("Description 1")
				.price(new BigDecimal("10.00")).stockQuantity(100).category("Category 1")
				.imageUrl("http://example.com/image1.jpg").build();

		ProductResponse product2 = ProductResponse.builder().id(2L).name("Product 2").description("Description 2")
				.price(new BigDecimal("20.00")).stockQuantity(200).category("Category 2")
				.imageUrl("http://example.com/image2.jpg").build();

		return List.of(product1, product2);
	}
}