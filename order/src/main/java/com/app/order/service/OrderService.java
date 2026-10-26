package com.app.order.service;

import com.app.order.client.UserServiceClient;
import com.app.order.dto.OrderResponse;
import com.app.order.dto.UserResponse;
import com.app.order.mapper.OrderMapper;
import com.app.order.model.CartItem;
import com.app.order.model.Order;
import com.app.order.model.OrderItem;
import com.app.order.model.OrderStatus;
import com.app.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderService {

	private final OrderRepository orderRepository;
	private final CartService cartService;
	private final UserServiceClient userServiceClient;
	private final OrderMapper orderMapper;

	public Optional<OrderResponse> createOrder(String userId) {
		List<CartItem> cartItems = cartService.fetchUserCart(userId);
		if (cartItems.isEmpty()) {
			return Optional.empty();
		}

		UserResponse userDetails = userServiceClient.getUserDetails(userId);
		if(Objects.isNull(userDetails)){
			return Optional.empty();
		}

		BigDecimal totalPrice = cartItems.stream().map(CartItem::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);

		Order order = Order.builder()
				.userId(userDetails.getId())
				.status(OrderStatus.CONFIRMED)
				.amount(totalPrice)
				.build();

		List<OrderItem> orderItems = cartItems.stream().map(cartItem -> new OrderItem(null, cartItem.getProductId(),
				cartItem.getQuantity(), cartItem.getPrice(), order)).toList();

		order.setItems(orderItems);
		Order savedOrder = orderRepository.save(order);

		cartService.clearCart(userId);
		return Optional.of(orderMapper.mapToOrderResponse(savedOrder));
	}
}
