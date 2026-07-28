package com.ecom.app.user.controller;


import com.ecom.app.user.dto.UserRequest;
import com.ecom.app.user.dto.UserResponse;
import com.ecom.app.user.mapper.UserMapper;
import com.ecom.app.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/users")
public class UserController {

	private final UserService userService;
	private final UserMapper userMapper;

	@GetMapping()
	public ResponseEntity<List<UserResponse>> getAllUsers() {
		return ResponseEntity.ok(userService.fetchAllUsers().stream().map(userMapper::mapToUserResponse).toList());
	}

	@PostMapping()
	public ResponseEntity<String> createUser(@RequestBody UserRequest user) {
		userService.addUser(userMapper.mapFromUserRequest(user));
		return new ResponseEntity<>("User added successfully", HttpStatus.OK);
	}

	@GetMapping("/{id}")
	public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
		return userService.getUserById(id).map(userMapper::mapToUserResponse).map(ResponseEntity::ok)
				.orElseGet(() -> ResponseEntity.notFound().build());
	}

	@PutMapping("/{id}")
	public ResponseEntity<String> updateUser(@PathVariable Long id, @RequestBody UserRequest updatedUser) {
		boolean updated = userService.updateUser(id, updatedUser);
		return updated ? ResponseEntity.ok("User updated successfully") : ResponseEntity.notFound().build();
	}

}
