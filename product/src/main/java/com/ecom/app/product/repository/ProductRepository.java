package com.ecom.app.product.repository;

import com.ecom.app.product.model.Product;

import java.nio.channels.FileChannel;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

	List<Product> findByIsActiveTrue();

	@Query("SELECT p FROM Product p WHERE p.isActive = true AND p.stockQuantity > 0 AND LOWER(p.name) LIKE LOWER(CONCAT('%',:keyword, '%'))")
	List<Product> searchByKeyword(@Param("keyword") String keyword);

	Optional<Product> findByIdAndActiveTrue(Long id);
}
