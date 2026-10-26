package com.app.order.mapper;

import com.app.order.dto.OrderItemDto;
import com.app.order.dto.OrderResponse;
import com.app.order.model.Order;
import com.app.order.model.OrderItem;
import com.app.order.model.OrderStatus;
import jakarta.persistence.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class OrderMapperTest {

    @Autowired
    private OrderMapper orderMapper;

    @Test
    void shouldMapToOrderResponse() {
        // given
        Order order = mockTestOrder();
        // when
        OrderResponse orderResponse = orderMapper.mapToOrderResponse(order);
        // then
        assertThat(order.getId()).isEqualTo(orderResponse.getId());
        assertThat(order.getCreatedAt()).isEqualTo(orderResponse.getCreatedAt());
        assertThat(order.getAmount()).isEqualTo(orderResponse.getTotalAmount());
        assertThat(order.getStatus()).isEqualTo(orderResponse.getStatus());
        assertThat(orderResponse.getItems()).hasSize(1);
    }

    @Test
    void shouldMapOrderItemToOrderItemDto(){
        //Given
        Order order = mockTestOrder();
        List<OrderItem> items = order.getItems();
        OrderItem item = items.get(0);

        //When
        OrderResponse orderResponse = orderMapper.mapToOrderResponse(order);
        List<OrderItemDto> orderItemDto = orderResponse.getItems();
        OrderItemDto dtoFirst = orderItemDto.get(0);
        //Then
        assertThat(items.size()).isEqualTo(orderItemDto.size());
        assertThat(item).usingRecursiveComparison().
                ignoringFields("subtotal","order").isEqualTo(dtoFirst);
    }

    private Order mockTestOrder() {
        return Order.builder()
                .id(12345L)
                .amount(BigDecimal.TEN)
                .status(OrderStatus.CONFIRMED)
                .items(List.of(mockTestOrderItem()))
                .userId("283437359759357L")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now()).build();
    }

    private OrderItem mockTestOrderItem() {
        return OrderItem.builder().id(34564L)
                .order(new Order())
                .productId("6")
                .price(BigDecimal.valueOf(500L))
                .quantity(10).build();
    }
}