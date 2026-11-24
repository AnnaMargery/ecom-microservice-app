package com.app.order.controller;

import com.app.order.dto.CartItemRequest;
import com.app.order.model.CartItem;
import com.app.order.service.CartService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CartController.class)
class CartControllerTest {

    private static final String USER_ID = "user-123";
    private static final String PRODUCT_ID = "product-456";
    public static final String X_USER_ID = "X-User-ID";
    public static final String API_CART = "/api/cart";
    public static final String ITEMS_PRODUCT_ID = "/items/{productId}";

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    CartService cartService;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void shouldAddToCart() throws Exception {
        CartItemRequest request = mockCartItemRequest();
        when(cartService.addToCart(USER_ID, request)).thenReturn(true);

        mockMvc.perform(post(API_CART)
                        .header(X_USER_ID, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().string(""));

        verify(cartService).addToCart(USER_ID, request);
    }

    @Test
    void shouldReturnBadRequestWhenCartItemCannotBeAdded() throws Exception {
        CartItemRequest request = mockCartItemRequest();
        when(cartService.addToCart(USER_ID, request)).thenReturn(false);

        mockMvc.perform(post(API_CART)
                        .header(X_USER_ID, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(
                        "Not able to complete request | Product out of stock or User not found or Product not found"));

        verify(cartService).addToCart(USER_ID, request);
    }

    @Test
    void shouldRemoveItemFromCart() throws Exception {
        when(cartService.deleteItemFromCart(USER_ID, PRODUCT_ID)).thenReturn(true);

        mockMvc.perform(delete(API_CART + ITEMS_PRODUCT_ID, PRODUCT_ID)
                        .header(X_USER_ID, USER_ID))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(cartService).deleteItemFromCart(USER_ID, PRODUCT_ID);
    }

    @Test
    void shouldReturnNotFoundWhenCartItemDoesNotExist() throws Exception {
        when(cartService.deleteItemFromCart(USER_ID, PRODUCT_ID)).thenReturn(false);

        mockMvc.perform(delete(API_CART + ITEMS_PRODUCT_ID, PRODUCT_ID)
                        .header(X_USER_ID, USER_ID))
                .andExpect(status().isNotFound());

        verify(cartService).deleteItemFromCart(USER_ID, PRODUCT_ID);
    }

    @Test
    void shouldFetchUserCart() throws Exception {
        CartItem item = CartItem.builder()
                .id(1L)
                .userId(USER_ID)
                .productId(PRODUCT_ID)
                .quantity(2)
                .price(new BigDecimal("25.50"))
                .build();
        when(cartService.fetchUserCart(USER_ID)).thenReturn(List.of(item));

        mockMvc.perform(get(API_CART)
                        .header(X_USER_ID, USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(USER_ID))
                .andExpect(jsonPath("$[0].productId").value(PRODUCT_ID))
                .andExpect(jsonPath("$[0].quantity").value(2))
                .andExpect(jsonPath("$[0].price").value(25.50));

        verify(cartService).fetchUserCart(USER_ID);
    }

    @Test
    void shouldReturnEmptyListWhenUserCartIsEmpty() throws Exception {
        when(cartService.fetchUserCart(USER_ID)).thenReturn(List.of());

        mockMvc.perform(get(API_CART)
                        .header(X_USER_ID, USER_ID))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(cartService).fetchUserCart(USER_ID);
    }

    @Test
    void shouldReturnBadRequestWhenUserIdHeaderIsMissing() throws Exception {
        mockMvc.perform(get(API_CART))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenUserIdHeaderIsMissingForAddToCart() throws Exception {
        mockMvc.perform(post(API_CART)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockCartItemRequest())))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(cartService);
    }

    @Test
    void shouldReturnBadRequestWhenUserIdHeaderIsMissingForRemoveFromCart() throws Exception {
        mockMvc.perform(delete(API_CART + ITEMS_PRODUCT_ID, PRODUCT_ID))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(cartService);
    }

    @Test
    void shouldReturnBadRequestWhenAddToCartRequestBodyIsMalformed() throws Exception {
        mockMvc.perform(post(API_CART)
                        .header(X_USER_ID, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(cartService);
    }

    @Test
    void shouldRejectUnsupportedContentTypeWhenAddingToCart() throws Exception {
        mockMvc.perform(post(API_CART)
                        .header(X_USER_ID, USER_ID)
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("productId=product-456&quantity=2"))
                .andExpect(status().isUnsupportedMediaType());

        verifyNoInteractions(cartService);
    }

    @Test
    void shouldRejectAddToCartWhenProductIdIsMissing() throws Exception {
        CartItemRequest request = new CartItemRequest();
        request.setQuantity(2);
        when(cartService.addToCart(USER_ID, request)).thenReturn(true);

        mockMvc.perform(post(API_CART)
                        .header(X_USER_ID, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":2}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(cartService);
    }

    @Test
    void shouldRejectAddToCartWhenQuantityIsMissing() throws Exception {
        CartItemRequest request = new CartItemRequest();
        request.setProductId(PRODUCT_ID);
        when(cartService.addToCart(USER_ID, request)).thenReturn(true);

        mockMvc.perform(post(API_CART)
                        .header(X_USER_ID, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":\"" + PRODUCT_ID + "\"}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(cartService);
    }

    @Test
    void shouldRejectAddToCartWhenQuantityIsZero() throws Exception {
        CartItemRequest request = new CartItemRequest();
        request.setProductId(PRODUCT_ID);
        request.setQuantity(0);
        when(cartService.addToCart(USER_ID, request)).thenReturn(true);

        mockMvc.perform(post(API_CART)
                        .header(X_USER_ID, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":\"" + PRODUCT_ID + "\",\"quantity\":0}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(cartService);
    }

    @Test
    void shouldRejectAddToCartWhenQuantityIsNegative() throws Exception {
        CartItemRequest request = new CartItemRequest();
        request.setProductId(PRODUCT_ID);
        request.setQuantity(-1);
        when(cartService.addToCart(USER_ID, request)).thenReturn(true);

        mockMvc.perform(post(API_CART)
                        .header(X_USER_ID, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":\"" + PRODUCT_ID + "\",\"quantity\":-1}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(cartService);
    }

    private static @NonNull CartItemRequest mockCartItemRequest() {
        CartItemRequest request = new CartItemRequest();
        request.setProductId(PRODUCT_ID);
        request.setQuantity(2);
        return request;
    }
}