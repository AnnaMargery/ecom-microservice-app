package com.ecom.app.user.service;


import com.ecom.app.user.dto.UserRequest;
import com.ecom.app.user.mapper.AddressMapper;
import com.ecom.app.user.model.User;
import com.ecom.app.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;
	private final AddressMapper addressMapper;

	public List<User> fetchAllUsers() {
		return userRepository.findAll();
	}

	public void addUser(User user) {
		userRepository.save(user);
	}

	public Optional<User> getUserById(Long id) {
		return userRepository.findById(id);
	}

	@Transactional
	public boolean updateUser(Long id, UserRequest request) {
		return userRepository.findById(id).map(existingUser -> {
			User updatedUser = User.builder().id(existingUser.getId()).firstName(request.getFirstName())
					.lastName(request.getLastName()).email(request.getEmail()).phoneNumber(request.getPhoneNumber())
					.address(request.getAddress() != null
							? addressMapper.mapToAddressFromDto(request.getAddress())
							: existingUser.getAddress())
					.build();
			userRepository.save(updatedUser);
			return true;
		}).orElse(false);
	}
}
