package com.app.order.service;

import static com.app.order.model.Order.*;

import com.app.order.dto.OrderResponse;
import com.app.order.mapper.OrderMapper;
import com.app.order.model.CartItem;
import com.app.order.model.Order;
import com.app.order.model.OrderItem;
import com.app.order.model.OrderStatus;
import com.app.order.repository.OrderRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderService {

	private final OrderRepository orderRepository;
	private final CartService cartService;
	// private final UserRepository userRepository;
	private final OrderMapper orderMapper;

	public Optional<OrderResponse> createOrder(String userId) {
		List<CartItem> cartItems = cartService.fetchUserCart(userId);
		if (cartItems.isEmpty()) {
			return Optional.empty();
		}

		// Optional<User> userOpt = userRepository.findById(Long.valueOf(userId));
		// if (userOpt.isEmpty()) {
		// return Optional.empty();
		// }
		// User user = userOpt.get();

		BigDecimal totalPrice = cartItems.stream().map(CartItem::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);

		Order order = new Order();
		order.setUserId(userId);

		order.setStatus(OrderStatus.CONFIRMED);
		order.setAmount(totalPrice);

		List<OrderItem> orderItems = cartItems.stream().map(cartItem -> new OrderItem(null, cartItem.getProductId(),
				cartItem.getQuantity(), cartItem.getPrice(), order)).toList();

		order.setItems(orderItems);
		Order savedOrder = orderRepository.save(order);

		cartService.clearCart(userId);
		return Optional.of(orderMapper.mapToOrderResponse(savedOrder));
	}
}
