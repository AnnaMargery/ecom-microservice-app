package com.app.order.controller;

import com.app.order.dto.CartItemRequest;
import com.app.order.dto.OrderResponse;
import com.app.order.model.CartItem;
import com.app.order.service.CartService;
import com.app.order.service.OrderService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

	private final OrderService orderService;

	@PostMapping
	public ResponseEntity<OrderResponse> createOrder(@RequestHeader("X-User_ID") String userId) {
		return orderService.createOrder(userId)
				.map(orderResponse -> new ResponseEntity<>(orderResponse, HttpStatus.CREATED))
				.orElseGet(() -> ResponseEntity.badRequest().build());
	}

	@RestController
	@RequestMapping("api/order")
	@RequiredArgsConstructor
	public static class CartController {

		private final CartService cartService;

		@PostMapping
		private ResponseEntity<String> addToCart(@RequestHeader("X-User-ID") String userId,
				@RequestBody CartItemRequest request) {
			if (!cartService.addToCart(userId, request)) {
				return ResponseEntity.badRequest().body("Product out of stock or User not found or Product not found");
			}
			return ResponseEntity.status(HttpStatus.CREATED).build();
		}

		@DeleteMapping("/items/{productId}")
		private ResponseEntity<Void> removeFromCart(@RequestHeader("X-User-ID") String userId,
				@PathVariable String productId) {
			boolean deleted = cartService.deleteItemFromCart(userId, productId);
			return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
		}

		@GetMapping()
		private ResponseEntity<List<CartItem>> fetchUserCart(@RequestHeader("X-User-ID") String userId) {
			return ResponseEntity.ok(cartService.fetchUserCart(userId));
		}

	}
}
