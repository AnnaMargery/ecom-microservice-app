package com.app.order.service;

import com.app.order.client.UserServiceClient;
import com.app.order.dto.OrderResponse;
import com.app.order.dto.UserResponse;
import com.app.order.mapper.OrderMapper;
import com.app.order.model.CartItem;
import com.app.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.*;


@SpringBootTest
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private CartService cartService;
    @Mock
    private UserServiceClient userServiceClient;
    @Mock
    private OrderMapper orderMapper;
    @InjectMocks
    private OrderService orderService;
    private String userId;
    @Mock
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        userId = "user-123";
        userResponse.setId(userId);
    }

    @Test
    void shouldReturnEmptyOptionalWhenUserNotFound() {
        // Given
        when(cartService.fetchUserCart(userId)).thenReturn(List.of(mockTestCartItem()));
        when(userServiceClient.getUserDetails(userId)).thenReturn(null);
        // When
        Optional<OrderResponse> result = orderService.createOrder(userId);
        // Then
        assertThat(result).isEqualTo(Optional.empty());
        verify(orderRepository, never()).save(any());
        verify(cartService, never()).clearCart(any());
    }

    private CartItem mockTestCartItem() {
        return CartItem.builder()
                .userId("user-audit-123")
                .productId("prod-999")
                .quantity(1)
                .price(BigDecimal.valueOf(150.00))
                .build();
    }
}