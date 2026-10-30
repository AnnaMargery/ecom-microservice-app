package com.ecom.app.product.validation;

import com.ecom.app.product.dto.ProductRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class ProductRequestValidator {

	private final Validator validator;

	public ProductRequestValidator(Validator validator) {
		this.validator = validator;
	}

	public void validate(ProductRequest request) {
		if (request == null) {
			throw new IllegalArgumentException("Product request is required");
		}

		Set<ConstraintViolation<ProductRequest>> violations = validator.validate(request);
		if (!violations.isEmpty()) {
			throw new ConstraintViolationException(violations);
		}
	}
}

