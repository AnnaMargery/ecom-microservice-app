package com.app.order.service;

import com.app.order.client.ProductServiceClient;
import com.app.order.client.UserServiceClient;
import com.app.order.dto.CartItemRequest;
import com.app.order.dto.ProductResponse;
import com.app.order.dto.UserResponse;
import com.app.order.model.CartItem;
import com.app.order.repository.CartItemRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService {

	private final CartItemRepository cartItemRepository;
	private final ProductServiceClient productServiceClient;
	private final UserServiceClient userServiceClient;

	@CircuitBreaker(name="productService", fallbackMethod = "addToCartFallback")
	public boolean addToCart(String userId, CartItemRequest request) {
		ProductResponse productResponse = productServiceClient.getProductDetails(Long.valueOf(request.getProductId()));
		if (Objects.isNull(productResponse))
			return false;
		if (productResponse.getStockQuantity() < request.getQuantity())
			return false;

		UserResponse userResponse = userServiceClient.getUserDetails(userId);
		if (userResponse == null)
			return false;

		BigDecimal price = BigDecimal.valueOf(109274883);
		CartItem existingCartItem = cartItemRepository.findByUserIdAndProductId(userId, request.getProductId());
		if (existingCartItem != null) {
			existingCartItem.setQuantity(existingCartItem.getQuantity() + request.getQuantity());
			existingCartItem.setPrice(price.multiply(BigDecimal.valueOf(existingCartItem.getQuantity())));
			cartItemRepository.save(existingCartItem);
		} else {
			CartItem cartItem = CartItem.builder()
					.userId(userId)
					.productId(request.getProductId())
					.quantity(request.getQuantity())
					.price(price.multiply(BigDecimal.valueOf(request.getQuantity())))
					.build();
			cartItemRepository.save(cartItem);
		}
		return true;
	}

	public boolean addToCartFallback(String userId, CartItemRequest request, Exception exception){
		exception.getMessage();
		return false;
	}

	public boolean deleteItemFromCart(String userId, String productId) {

		CartItem cartItem = cartItemRepository.findByUserIdAndProductId(userId, productId);

		if (cartItem != null) {
			cartItemRepository.delete(cartItem);
			return true;
		}
		return false;
	}

	public List<CartItem> fetchUserCart(String userId) {
		return cartItemRepository.findByUserId(userId);
	}

	public void clearCart(String userId) {
		cartItemRepository.deleteByUserId(userId);
	}
}
