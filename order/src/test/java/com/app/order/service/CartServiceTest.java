package com.app.order.service;

import com.app.order.client.ProductServiceClient;
import com.app.order.client.UserServiceClient;
import com.app.order.dto.CartItemRequest;
import com.app.order.dto.ProductResponse;
import com.app.order.dto.UserResponse;
import com.app.order.model.CartItem;
import com.app.order.repository.CartItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartItemRepository cartItemRepository;
    @Mock
    private ProductServiceClient productServiceClient;
    @Mock
    private UserServiceClient userServiceClient;
    @InjectMocks
    private CartService cartService;

    private ProductResponse productResponse;
    private String userId;
    @Mock
    private CartItemRequest request;
    @Mock
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        userId = "user-123";
        request = new CartItemRequest();
        request.setProductId("55");
        request.setQuantity(2);
        productResponse = mockProductResponse();
        productResponse.setStockQuantity(10);
    }

    @Test
    void shouldAddToCartWhenItemDoesNotExistYet() {
        // Given
        when(productServiceClient.getProductDetails(55L)).thenReturn(productResponse);
        when(userServiceClient.getUserDetails(userId)).thenReturn(userResponse);
        when(cartItemRepository.findByUserIdAndProductId(userId, "55")).thenReturn(null);
        // When
        boolean result = cartService.addToCart(userId, request);
        // Then
        assertThat(result).isTrue();

        ArgumentCaptor<CartItem> captor = ArgumentCaptor.forClass(CartItem.class);
        verify(cartItemRepository).save(captor.capture());

        CartItem savedItem = captor.getValue();
        assertThat(savedItem.getUserId()).isEqualTo(userId);
        assertThat(savedItem.getProductId()).isEqualTo("55");
        assertThat(savedItem.getQuantity()).isEqualTo(2);
        assertThat(savedItem.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(218549766));
    }

    @Test
    void shouldUpdateCartWhenItemAlreadyExists() {
        // Given
        when(productServiceClient.getProductDetails(55L)).thenReturn(productResponse);
        when(userServiceClient.getUserDetails(userId)).thenReturn(userResponse);

        CartItem existingItem = CartItem.builder()
                .id(1L)
                .userId(userId)
                .productId("55")
                .quantity(1)
                .price(BigDecimal.valueOf(109274883))
                .build();
        when(cartItemRepository.findByUserIdAndProductId(userId, "55")).thenReturn(existingItem);
        // When
        boolean result = cartService.addToCart(userId, request);
        // Then
        assertThat(result).isTrue();
        assertThat(existingItem.getQuantity()).isEqualTo(3);
        verify(cartItemRepository).save(existingItem);
    }

    @Test
    void shouldReturnFalseWhenProductIsNull() {
        // Given
        when(productServiceClient.getProductDetails(55L)).thenReturn(null);
        // When
        boolean result = cartService.addToCart(userId, request);
        // Then
        assertThat(result).isFalse();
        verifyNoInteractions(userServiceClient);
        verify(cartItemRepository, never()).save(any());
    }
    @Test
    void shouldReturnFalseWhenStockIsInsufficient() {
        // Given
        productResponse.setStockQuantity(1);
        when(productServiceClient.getProductDetails(55L)).thenReturn(productResponse);
        // When
        boolean result = cartService.addToCart(userId, request);
        // Then
        assertThat(result).isFalse();
        verifyNoInteractions(userServiceClient);
        verify(cartItemRepository, never()).save(any());
    }

    @Test
    void shouldReturnFalseWhenUserIsNull() {
        // Given
        when(productServiceClient.getProductDetails(55L)).thenReturn(productResponse);
        when(userServiceClient.getUserDetails(userId)).thenReturn(null);
        // When
        boolean result = cartService.addToCart(userId, request);
        // Then
        assertThat(result).isFalse();
        verify(cartItemRepository, never()).save(any());
    }
    @Test
    void shouldDeleteItemFromCartWhenItemExists() {
        // Given
        CartItem cartItem = new CartItem();
        when(cartItemRepository.findByUserIdAndProductId(userId, "55")).thenReturn(cartItem);
        // When
        boolean result = cartService.deleteItemFromCart(userId, "55");
        // Then
        assertThat(result).isTrue();
        verify(cartItemRepository).delete(cartItem);
    }

    @Test
    void shouldReturnFalseWhenDeletingNonExistingItem() {
        // Given
        when(cartItemRepository.findByUserIdAndProductId(userId, "55")).thenReturn(null);
        // When
        boolean result = cartService.deleteItemFromCart(userId, "55");
        // Then
        assertThat(result).isFalse();
        verify(cartItemRepository, never()).delete(any());
    }

    @Test
    void shouldFetchUserCart() {
        // Given
        List<CartItem> mockList = List.of(new CartItem(), new CartItem());
        when(cartItemRepository.findByUserId(userId)).thenReturn(mockList);
        // When
        List<CartItem> result = cartService.fetchUserCart(userId);
        // Then
        assertThat(result).hasSize(2);
        verify(cartItemRepository).findByUserId(userId);
    }

    @Test
    void shouldClearCart() {
        // When
        cartService.clearCart(userId);
        // Then
        verify(cartItemRepository).deleteByUserId(userId);
    }

    private ProductResponse mockProductResponse(){
            return ProductResponse.builder()
                    .id(2222222L)
                    .name("Product name")
                    .description("Product Description")
                    .category("Product category")
                    .imageUrl("hhtp://productimage.com")
                    .price(BigDecimal.valueOf(11111L))
                    .isActive(true)
                    .stockQuantity(2)
                    .build();
    }
}