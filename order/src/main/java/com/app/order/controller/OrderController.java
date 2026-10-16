package com.app.order.controller;

import com.app.order.dto.CartItemRequest;
import com.app.order.dto.OrderResponse;
import com.app.order.model.CartItem;
import com.app.order.service.CartService;
import com.app.order.service.OrderService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

	private final OrderService orderService;

	@PostMapping
	public ResponseEntity<OrderResponse> createOrder(@RequestHeader("X-User-ID") String userId) {
		log.info("Creating an order for user: {}", userId);
		return orderService.createOrder(userId)
				.map(orderResponse -> new ResponseEntity<>(orderResponse, HttpStatus.CREATED))
				.orElseGet(() -> ResponseEntity.badRequest().build());

	}
}
