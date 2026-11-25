package com.ecom.app.user.controller;

import com.ecom.app.user.dto.AddressDto;
import com.ecom.app.user.dto.UserRequest;
import com.ecom.app.user.dto.UserResponse;
import com.ecom.app.user.model.User;
import com.ecom.app.user.mapper.UserMapper;
import com.ecom.app.user.model.UserRole;
import com.ecom.app.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@WebMvcTest(UserController.class)
class UserControllerTest {

	public static final String API_USERS = "/api/users";
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private UserService userService;

	@MockitoBean
	private UserMapper userMapper;

	@Test
	void shouldAddValidUser() throws Exception {
		AddressDto validAddress = mockValidAddress();
		UserRequest userRequest = mockUserRequest(validAddress);
		User mappedUser = mockUser(userRequest);
		when(userMapper.mapFromUserRequest(userRequest)).thenReturn(mappedUser);

		mockMvc.perform(post(API_USERS)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(userRequest)))
				.andExpect(status().isCreated())
				.andExpect(content().string("User added successfully"));

		verify(userMapper).mapFromUserRequest(userRequest);
		verify(userService).addUser(mappedUser);
	}

	@ParameterizedTest(name = "{index} - {0}")
	@MethodSource("invalidUserProvider")
	void shouldNotAddInvalidUser(String message, UserRequest userRequest) throws Exception {

			mockMvc.perform(post(API_USERS)
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(userRequest)))
					.andExpect(status().isBadRequest());

		verifyNoInteractions(userService);
	}

	@Test
	void shouldGetAllExistingUsers() throws Exception {
		User firstUser = mockUser("user-1", "Ada", "Lovelace");
		User secondUser = mockUser("user-2", "Grace", "Hopper");
		UserResponse firstResponse = mockUserResponse(firstUser);
		UserResponse secondResponse = mockUserResponse(secondUser);
		when(userService.fetchAllUsers()).thenReturn(List.of(firstUser, secondUser));
		when(userMapper.mapToUserResponse(firstUser)).thenReturn(firstResponse);
		when(userMapper.mapToUserResponse(secondUser)).thenReturn(secondResponse);

		mockMvc.perform(get(API_USERS))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[0].id").value("user-1"))
				.andExpect(jsonPath("$[0].firstName").value("Ada"))
				.andExpect(jsonPath("$[1].id").value("user-2"))
				.andExpect(jsonPath("$[1].firstName").value("Grace"));

		verify(userService).fetchAllUsers();
		verify(userMapper).mapToUserResponse(firstUser);
		verify(userMapper).mapToUserResponse(secondUser);
	}

	@Test
	void shouldGetEmptyListWhenNoUsers() throws Exception {
		when(userService.fetchAllUsers()).thenReturn(List.of());

		mockMvc.perform(get(API_USERS))
				.andExpect(status().isOk())
				.andExpect(content().json("[]"));

		verify(userService).fetchAllUsers();
		verifyNoInteractions(userMapper);
	}

	@Test
	void shouldGetUserByValidId() throws Exception {
		User user = mockUser("user-1", "Ada", "Lovelace");
		user.setEmail("ada@example.com");
		user.setPhoneNumber("1234567890");
		UserResponse response = mockUserResponse(user);
		when(userService.getUserById("user-1")).thenReturn(Optional.of(user));
		when(userMapper.mapToUserResponse(user)).thenReturn(response);

		mockMvc.perform(get(API_USERS + "/{id}", "user-1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value("user-1"))
				.andExpect(jsonPath("$.firstName").value("Ada"))
				.andExpect(jsonPath("$.email").value("ada@example.com"))
				.andExpect(jsonPath("$.role").value("CUSTOMER"));

		verify(userService).getUserById("user-1");
		verify(userMapper).mapToUserResponse(user);
	}

	@Test
	void shouldReturnNotFoundWhenUserIdDoesNotExist() throws Exception {
		when(userService.getUserById("missing-user")).thenReturn(Optional.empty());

		mockMvc.perform(get(API_USERS + "/{id}", "missing-user"))
				.andExpect(status().isNotFound());

		verify(userService).getUserById("missing-user");
		verifyNoInteractions(userMapper);
	}

	@Test
	void shouldUpdateUser() throws Exception {
		UserRequest userRequest = mockUserRequest(mockValidAddress());
		when(userService.updateUser("user-1", userRequest)).thenReturn(true);

		mockMvc.perform(put(API_USERS + "/{id}", "user-1")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(userRequest)))
				.andExpect(status().isOk())
				.andExpect(content().string("User updated successfully"));

		verify(userService).updateUser("user-1", userRequest);
		verifyNoInteractions(userMapper);
	}

	@Test
	void shouldReturnNotFoundWhenUpdatingUserThatDoesNotExist() throws Exception {
		UserRequest userRequest = mockUserRequest(mockValidAddress());
		when(userService.updateUser("missing-user", userRequest)).thenReturn(false);

		mockMvc.perform(put(API_USERS + "/{id}", "missing-user")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(userRequest)))
				.andExpect(status().isNotFound());

		verify(userService).updateUser("missing-user", userRequest);
	}

	private User mockUser(UserRequest userRequest) {
		return User.builder()
				.firstName(userRequest.getFirstName())
				.lastName(userRequest.getLastName())
				.email(userRequest.getEmail())
				.phoneNumber(userRequest.getPhoneNumber())
				.build();
	}

	private User mockUser(String id, String firstName, String lastName) {
		return User.builder()
				.id(id)
				.firstName(firstName)
				.lastName(lastName)
				.build();
	}

	private UserResponse mockUserResponse(User user) {
		return UserResponse.builder()
				.id(user.getId())
				.firstName(user.getFirstName())
				.lastName(user.getLastName())
				.email(user.getEmail())
				.phoneNumber(user.getPhoneNumber())
				.role(UserRole.CUSTOMER)
				.build();
	}

	private UserRequest mockUserRequest(AddressDto validAddress) {
		return mockUserRequest("John", "Doe", "john.doe@example.com", "1234567890", validAddress);
	}

	private UserRequest mockUserRequest(String firstName, String lastName, String email,
			String phoneNumber, AddressDto address) {
		return UserRequest.builder()
				.firstName(firstName)
				.lastName(lastName)
				.email(email)
				.phoneNumber(phoneNumber)
				.address(address)
				.build();
	}

	private AddressDto mockValidAddress() {
		return AddressDto.builder()
				.street("123 Main St")
				.city("City")
				.state("State")
				.country("Country")
				.zipcode("12345")
				.build();
	}

	private Stream<Arguments> invalidUserProvider() {
		AddressDto validAddress = mockValidAddress();

		AddressDto invalidAddress = new AddressDto(
				"123 Main St",
				"City",
				null,
				"Country",
				""
		);

		return Stream.of(
				Arguments.of("Invalid email", mockUserRequest("John", "Doe", "invalid-email", "1234567890", validAddress)),
				Arguments.of("Invalid phone number", mockUserRequest("John", "Doe", "john.doe@example.com", "12345A", validAddress)),
				Arguments.of("Empty first name", mockUserRequest("", "Doe", "john.doe@example.com", "1234567890", validAddress)),
				Arguments.of("Empty last name", mockUserRequest("John", "", "john.doe@example.com", "1234567890", validAddress)),
				Arguments.of("Empty email", mockUserRequest("John", "Doe", "", "1234567890", validAddress)),
				Arguments.of("Empty phone number", mockUserRequest("John", "Doe", "john.doe@example.com", "", validAddress)),
				Arguments.of("Invalid address", mockUserRequest("John", "Doe", "john.doe@example.com", "1234567890", invalidAddress))
		);
	}
}
