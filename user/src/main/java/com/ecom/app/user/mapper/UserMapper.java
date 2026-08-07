package com.ecom.app.user.mapper;

import com.ecom.app.user.dto.UserRequest;
import com.ecom.app.user.dto.UserResponse;
import com.ecom.app.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMapper {

	private final AddressMapper addressMapper;

	public UserResponse mapToUserResponse(User user) {
		return UserResponse.builder().id(user.getId() != null ? String.valueOf(user.getId()) : null)
				.firstName(user.getFirstName()).lastName(user.getLastName()).role(user.getRole()).email(user.getEmail())
				.phoneNumber(user.getPhoneNumber())
				.address(user.getAddress() != null ? addressMapper.mapToAddressDto(user.getAddress()) : null).build();
	}

	public User mapFromUserRequest(UserRequest userRequest) {
		return User.builder().firstName(userRequest.getFirstName()).lastName(userRequest.getLastName())
				.email(userRequest.getEmail()).phoneNumber(userRequest.getPhoneNumber())
				.address(userRequest.getAddress() != null
						? addressMapper.mapToAddressFromDto(userRequest.getAddress())
						: null)
				.build();

	}
}
