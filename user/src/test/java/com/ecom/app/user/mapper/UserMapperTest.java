package com.ecom.app.user.mapper;

import com.ecom.app.user.dto.AddressDto;
import com.ecom.app.user.dto.UserRequest;
import com.ecom.app.user.dto.UserResponse;
import com.ecom.app.user.model.Address;
import com.ecom.app.user.model.User;
import com.ecom.app.user.model.UserRole;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserMapperTest {

    @Autowired
    private UserMapper userMapper;

    @Test
    void mapToUserResponse() {
        //Given
        User user = mockUser();
        //When
        UserResponse userResponse = userMapper.mapToUserResponse(user);

        //Then
        assertThat(userResponse)
                .usingRecursiveComparison().
                ignoringFields("id","address","role","createdAt","updatedAt")
                .isEqualTo(user);
        assertThat(userResponse.getAddress()).usingRecursiveComparison()
                .ignoringFields("id").isEqualTo(user.getAddress());

    }

    @Test
    void mapFromUserRequest() {
        //Given
        UserRequest userRequest = mockUserRequest();

        //When
        User user = userMapper.mapFromUserRequest(userRequest);

        //Then
        assertThat(user)
                .usingRecursiveComparison().
                ignoringFields("id","address","role","createdAt","updatedAt")
                .isEqualTo(userRequest);
        assertThat(user.getAddress()).usingRecursiveComparison()
                .ignoringFields("id").isEqualTo(userRequest.getAddress());

    }

    private User mockUser(){
        return User.builder()
                .id("111111")
                .firstName("Anna")
                .lastName("Johnson")
                .address(mockAddress())
                .role(UserRole.ADMIN)
                .email("abc@abc")
                .phoneNumber("123456")
                .createdAt(LocalDateTime.MIN)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private UserRequest mockUserRequest(){
        return UserRequest.builder()
                .firstName("Anna")
                .lastName("Johnson")
                .address(mockDtoAddress())
                .email("abc@abc")
                .phoneNumber("123456")
                .build();
    }

    private AddressDto mockDtoAddress(){
        return AddressDto.builder()
                .street("123 Happy Rue")
                .city("Happy Town")
                .zipcode("22222")
                .country("Dummy")
                .state("State")
                .build();
    }

    private Address mockAddress(){
        return Address.builder()
                .id("22222")
                .street("123 Happy Rue")
                .city("Happy Town")
                .zipcode("22222")
                .state("State")
                .country("Dummy")
                .build();
    }
}