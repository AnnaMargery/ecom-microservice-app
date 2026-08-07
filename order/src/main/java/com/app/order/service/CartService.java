package com.app.order.service;

import com.app.order.dto.CartItemRequest;
import com.app.order.model.CartItem;
import com.app.order.repository.CartItemRepository;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService {

	private final CartItemRepository cartItemRepository;
	// private final ProductRepository productRepository;
	// private final UserRepository userRepository;

	public boolean addToCart(String userId, CartItemRequest request) {
		// Optional<Product> productOpt =
		// productRepository.findById(request.getProductId());
		// if (productOpt.isEmpty())
		// return false;
		//
		// Product product = productOpt.get();
		// if (product.getStockQuantity() < request.getQuantity())
		// return false;
		//
		// Optional<User> userOpt = userRepository.findById(Long.valueOf(userId));
		// if (userOpt.isEmpty())
		// return false;
		//
		// User user = userOpt.get();

		BigDecimal price = BigDecimal.valueOf(109274883);
		CartItem existingCartItem = cartItemRepository.findByUserIdAndProductId(userId, request.getProductId());
		if (existingCartItem != null) {
			existingCartItem.setQuantity(existingCartItem.getQuantity() + request.getQuantity());
			existingCartItem.setPrice(price.multiply(BigDecimal.valueOf(existingCartItem.getQuantity())));
			cartItemRepository.save(existingCartItem);
		} else {
			CartItem cartItem = new CartItem();
			cartItem.setUserId(userId);
			cartItem.setProductId(request.getProductId());
			cartItem.setQuantity(request.getQuantity());
			cartItem.setPrice(price.multiply(BigDecimal.valueOf(request.getQuantity())));
			cartItemRepository.save(cartItem);
		}
		return true;
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
