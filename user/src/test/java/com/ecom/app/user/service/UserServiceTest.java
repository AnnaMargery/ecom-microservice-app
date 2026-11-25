package com.ecom.app.user.service;

import com.ecom.app.user.dto.AddressDto;
import com.ecom.app.user.dto.UserRequest;
import com.ecom.app.user.mapper.AddressMapper;
import com.ecom.app.user.model.Address;
import com.ecom.app.user.model.User;
import com.ecom.app.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

	private static final String USER_ID = "user-123";

	@Mock
	private UserRepository userRepository;

	@Mock
	private AddressMapper addressMapper;

	@InjectMocks
	private UserService userService;

	@Test
	void shouldFetchAllUsers() {
		List<User> users = List.of(mockUser(USER_ID), mockUser("user-456"));
		when(userRepository.findAll()).thenReturn(users);

		List<User> result = userService.fetchAllUsers();

		assertThat(result).containsExactlyElementsOf(users);
		verify(userRepository).findAll();
	}

	@Test
	void shouldReturnEmptyListWhenNoUsersExist() {
		when(userRepository.findAll()).thenReturn(List.of());

		List<User> result = userService.fetchAllUsers();

		assertThat(result).isEmpty();
		verify(userRepository).findAll();
	}

	@Test
	void shouldAddUser() {
		User user = mockUser(USER_ID);

		userService.addUser(user);

		verify(userRepository).save(user);
	}

	@Test
	void shouldGetUserByIdWhenUserExists() {
		User user = mockUser(USER_ID);
		when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

		Optional<User> result = userService.getUserById(USER_ID);

		assertThat(result).contains(user);
		verify(userRepository).findById(USER_ID);
	}

	@Test
	void shouldReturnEmptyWhenUserDoesNotExist() {
		when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

		Optional<User> result = userService.getUserById(USER_ID);

		assertThat(result).isEmpty();
		verify(userRepository).findById(USER_ID);
	}

	@Test
	void shouldUpdateUserAndMapProvidedAddress() {
		User existingUser = mockUser(USER_ID);
		AddressDto addressDto = mockAddressDto();
		Address updatedAddress = Address.builder().street("New Street").build();
		UserRequest request = mockUserRequest(addressDto);
		when(userRepository.findById(USER_ID)).thenReturn(Optional.of(existingUser));
		when(addressMapper.mapToAddressFromDto(addressDto)).thenReturn(updatedAddress);

		boolean result = userService.updateUser(USER_ID, request);

		assertThat(result).isTrue();
		ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
		verify(userRepository).save(userCaptor.capture());
		User savedUser = userCaptor.getValue();
		assertThat(savedUser.getId()).isEqualTo(USER_ID);
		assertThat(savedUser.getFirstName()).isEqualTo(request.getFirstName());
		assertThat(savedUser.getLastName()).isEqualTo(request.getLastName());
		assertThat(savedUser.getEmail()).isEqualTo(request.getEmail());
		assertThat(savedUser.getPhoneNumber()).isEqualTo(request.getPhoneNumber());
		assertThat(savedUser.getAddress()).isSameAs(updatedAddress);
		verify(addressMapper).mapToAddressFromDto(addressDto);
	}

	@Test
	void shouldPreserveExistingAddressWhenUpdateRequestHasNoAddress() {
		Address existingAddress = Address.builder().street("Existing Street").build();
		User existingUser = mockUser(USER_ID);
		existingUser.setAddress(existingAddress);
		UserRequest request = mockUserRequest(null);
		when(userRepository.findById(USER_ID)).thenReturn(Optional.of(existingUser));

		boolean result = userService.updateUser(USER_ID, request);

		assertThat(result).isTrue();
		ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
		verify(userRepository).save(userCaptor.capture());
		assertThat(userCaptor.getValue().getAddress()).isSameAs(existingAddress);
		verifyNoInteractions(addressMapper);
	}

	@Test
	void shouldNotUpdateUserWhenUserDoesNotExist() {
		when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

		boolean result = userService.updateUser(USER_ID, mockUserRequest(mockAddressDto()));

		assertThat(result).isFalse();
		verify(userRepository).findById(USER_ID);
		verify(userRepository, never()).save(any(User.class));
		verifyNoInteractions(addressMapper);
	}

	private User mockUser(String id) {
		return User.builder()
				.id(id)
				.firstName("Ada")
				.lastName("Lovelace")
				.email("ada@example.com")
				.phoneNumber("1234567890")
				.build();
	}

	private UserRequest mockUserRequest(AddressDto address) {
		return UserRequest.builder()
				.firstName("Grace")
				.lastName("Hopper")
				.email("grace@example.com")
				.phoneNumber("9876543210")
				.address(address)
				.build();
	}

	private AddressDto mockAddressDto() {
		return AddressDto.builder()
				.street("New Street")
				.city("New City")
				.state("New State")
				.country("New Country")
				.zipcode("54321")
				.build();
	}
}
