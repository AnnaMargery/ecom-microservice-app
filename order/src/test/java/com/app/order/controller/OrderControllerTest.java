package com.app.order.controller;

import com.app.order.dto.OrderResponse;
import com.app.order.dto.OrderItemDto;
import com.app.order.model.OrderStatus;
import com.app.order.service.OrderService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;


import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@WebMvcTest(OrderController.class)
class OrderControllerTest {

    public static final String USER_ID = "user-123";
    public static final String API_ORDERS = "/api/orders";
    public static final String X_USER_ID = "X-User-ID";
    @Autowired
    MockMvc mockMvc;
    @MockitoBean
    private OrderService orderService;

    @Test
    void shouldCreateOrderWhenValidUserIdIsProvided() throws Exception {
        OrderResponse orderResponse = mockOrderResponse();
        when(orderService.createOrder(USER_ID)).thenReturn(Optional.of(orderResponse));

        mockMvc.perform(post(API_ORDERS)
                        .header(X_USER_ID, USER_ID))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(orderResponse.getId()))
                .andExpect(jsonPath("$.totalAmount").value(orderResponse.getTotalAmount()))
                .andExpect(jsonPath("$.status").value(orderResponse.getStatus().toString()))
                .andExpect(jsonPath("$.items[0].productId").value("product-456"))
                .andExpect(jsonPath("$.items[0].quantity").value(2))
                .andExpect(jsonPath("$.items[0].price").value(50.0))
                .andExpect(jsonPath("$.createdAt").value("2026-11-24T14:00:00"));

        verify(orderService).createOrder(USER_ID);
    }

    @Test
    void shouldReturnBadRequestWhenOrderCreationFails() throws Exception {
        when(orderService.createOrder(USER_ID)).thenReturn(Optional.empty());

        mockMvc.perform(post(API_ORDERS)
                        .header(X_USER_ID, USER_ID))
                .andExpect(status().isBadRequest());

        verify(orderService).createOrder(USER_ID);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidUserIds")
    void shouldReturnBadRequestWhenUserIdIsMissingOrBlank(String scenario, String userId) throws Exception {
        MockHttpServletRequestBuilder request = post(API_ORDERS);
        if (userId != null) {
            request.header(X_USER_ID, userId);
        }

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());

        verifyNoInteractions(orderService);
    }


    private static OrderResponse mockOrderResponse() {
        return  new OrderResponse(
                1L,
                new BigDecimal("100.0"),
                OrderStatus.CONFIRMED,
                List.of(OrderItemDto.builder()
                        .id(10L)
                        .productId("product-456")
                        .quantity(2)
                        .price(new BigDecimal("50.0"))
                        .subTotal(new BigDecimal("100.0"))
                        .build()),
                LocalDateTime.of(2026, 11, 24, 14, 0)
        );
    }

    public static Stream<Arguments> invalidUserIds() {
        return Stream.of(
                Arguments.of("User header is null", null),
                Arguments.of("User is blank"," "),
                Arguments.of("User is empty","")
        );
    }
}