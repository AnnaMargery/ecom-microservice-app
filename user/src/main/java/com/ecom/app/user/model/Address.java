package com.ecom.app.user.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class Address {

	private String id;
	private String street;
	private String city;
	private String state;
	private String country;
	private String zipcode;
}
