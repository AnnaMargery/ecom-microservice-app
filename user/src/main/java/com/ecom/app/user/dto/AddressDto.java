package com.ecom.app.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressDto {

	@NotBlank(message = "Street is required")
	@Size(min = 2, max = 100, message = "Street must be between 2 and 100 characters")
	private String street;
	@NotBlank(message = "City is required")
	@Size(min = 2, max = 100, message = "City must be between 2 and 100 characters")
	private String city;
	@NotBlank(message = "State is required")
	@Size(min = 2, max = 100, message = "State must be between 2 and 100 characters")
	private String state;
	@NotBlank(message = "Country is required")
	@Size(min = 2, max = 100, message = "Country must be between 2 and 100 characters")
	private String country;
	@NotBlank(message = "Zipcode is required")
	@Size(min = 5, max = 10, message = "Zipcode must be between 5 and 10 characters")
	private String zipcode;
}
