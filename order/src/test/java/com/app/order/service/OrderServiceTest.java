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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.Mockito.*;


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

    @BeforeEach
    void setUp() {
        userId = "user-123";
    }

    @Test
    void shouldReturnEmptyOptionalWhenCartIsEmpty() {
        when(cartService.fetchUserCart(userId)).thenReturn(List.of());

        Optional<OrderResponse> result = orderService.createOrder(userId);

        assertThat(result).isEmpty();
        verifyNoInteractions(userServiceClient, orderRepository, orderMapper);
        verify(cartService, never()).clearCart(any());
    }

    @Test
    void shouldReturnEmptyOptionalWhenUserNotFound() {
        // Given
        when(cartService.fetchUserCart(userId)).thenReturn(mockTestCartItem());
        when(userServiceClient.getUserDetails(userId)).thenReturn(null);
        // When
        Optional<OrderResponse> result = orderService.createOrder(userId);
        // Then
        assertThat(result).isEqualTo(Optional.empty());
        verify(orderRepository, never()).save(any());
        verify(cartService, never()).clearCart(any());
        verifyNoInteractions(orderMapper);
    }

    @Test
    void shouldCreateOrderAndClearCartAfterSavingOrder() {
        List<CartItem> cartItems = mockTestCartItem();
        OrderResponse expectedResponse = new OrderResponse(
                1L, new BigDecimal("19.75"), OrderStatus.CONFIRMED, List.of(), null);
        when(cartService.fetchUserCart(userId)).thenReturn(cartItems);
        when(userServiceClient.getUserDetails(userId))
                .thenReturn(UserResponse.builder().id(userId).build());
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderMapper.mapToOrderResponse(any(Order.class))).thenReturn(expectedResponse);

        Optional<OrderResponse> result = orderService.createOrder(userId);

        assertThat(result).contains(expectedResponse);
        assertSavedOrderAndSideEffectOrder();
    }

    private void assertSavedOrderAndSideEffectOrder() {
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        Order savedOrder = orderCaptor.getValue();

        assertThat(savedOrder.getUserId()).isEqualTo(userId);
        assertThat(savedOrder.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(savedOrder.getAmount()).isEqualByComparingTo("19.75");
        assertThat(savedOrder.getItems())
                .extracting(OrderItem::getProductId, OrderItem::getQuantity)
                .containsExactly(
                        tuple("product-1", 2),
                        tuple("product-2", 1));
        assertThat(savedOrder.getItems())
                .allSatisfy(item -> assertThat(item.getOrder()).isSameAs(savedOrder));

        InOrder inOrder = inOrder(orderRepository, cartService, orderMapper);
        inOrder.verify(orderRepository).save(savedOrder);
        inOrder.verify(cartService).clearCart(userId);
        inOrder.verify(orderMapper).mapToOrderResponse(savedOrder);
    }

    private List<CartItem> mockTestCartItem() {
        CartItem firstItem = CartItem.builder()
                .userId(userId)
                .productId("product-1")
                .quantity(2)
                .price(new BigDecimal("12.50"))
                .build();
        CartItem secondItem = CartItem.builder()
                .userId(userId)
                .productId("product-2")
                .quantity(1)
                .price(new BigDecimal("7.25"))
                .build();
        return List.of(firstItem, secondItem);
    }
}