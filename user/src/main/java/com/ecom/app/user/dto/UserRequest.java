package com.ecom.app.user.dto;

import lombok.Data;

@Data
public class UserRequest {

	private String firstName;
	private String lastName;
	private String email;
	private String phoneNumber;
	private AddressDto address;
}
