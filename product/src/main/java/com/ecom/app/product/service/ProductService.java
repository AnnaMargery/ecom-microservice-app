package com.ecom.app.product.service;

import com.ecom.app.product.dto.ProductRequest;
import com.ecom.app.product.dto.ProductResponse;
import com.ecom.app.product.mapper.ProductMapper;
import com.ecom.app.product.model.Product;
import com.ecom.app.product.repository.ProductRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {

	private final ProductRepository productRepository;
	private final ProductMapper productMapper;

	public ProductResponse createProduct(ProductRequest productRequest) {
		Product product = new Product();
		productMapper.updateProductFromRequest(product, productRequest);
		return productMapper.mapToProductResponse(productRepository.save(product));
	}

	public Optional<ProductResponse> updateProduct(Long id, ProductRequest productRequest) {
		return productRepository.findById(id).map(existingProduct -> {
			productMapper.updateProductFromRequest(existingProduct, productRequest);
			Product savedProduct = productRepository.save(existingProduct);
			return productMapper.mapToProductResponse(savedProduct);
		});
	}

	public List<ProductResponse> getAllProducts() {
		return productRepository.findByIsActiveTrue().stream().map(productMapper::mapToProductResponse).toList();
	}

	public boolean deleteProduct(Long id) {
		return productRepository.findById(id).map(product -> {
			product.setActive(false);
			productRepository.save(product);
			return true;
		}).orElse(false);
	}

	public List<ProductResponse> searchByKeyword(String keyword) {
		return productRepository.searchByKeyword(keyword).stream().map(productMapper::mapToProductResponse).toList();
	}
}
