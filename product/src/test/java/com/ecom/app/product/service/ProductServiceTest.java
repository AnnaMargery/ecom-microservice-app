package com.ecom.app.product.service;

import com.ecom.app.product.dto.ProductRequest;
import com.ecom.app.product.dto.ProductResponse;
import com.ecom.app.product.mapper.ProductMapper;
import com.ecom.app.product.model.Product;
import com.ecom.app.product.repository.ProductRepository;
import com.ecom.app.product.validation.ProductRequestValidator;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    public static final String PRODUCT_DESCRIPTION = "Dummy description";
    public static final String PRODUCT_CATEGORY = "Dummy category";
    public static final BigDecimal PRODUCT_PRICE = BigDecimal.valueOf(10L);
    public static final int STOCK_QUANTITY = 10;
    public static final String PRODUCT_URL = "https://dummyimage.com";
    @Mock
    private ProductRepository productRepository;
    @Mock
    private ProductMapper productMapper;
    private final ProductRequestValidator productRequestValidator = new ProductRequestValidator(
            Validation.buildDefaultValidatorFactory().getValidator());
    private ProductService productService;

    @BeforeEach
    void setUp() {
        productService = new ProductService(productRepository, productMapper, productRequestValidator);
    }

    private final Long PRODUCT_ID = 1L;
    private final String PRODUCT_NAME = "Dummy product";


    @Test
    void shouldCreateProduct() {
        // Given
        ProductRequest request = mockProductRequest();
        Product savedProduct = mockSavedProduct(request, PRODUCT_ID);
        ProductResponse expectedResponse = mockProductResponse(request, PRODUCT_ID);
        mockMapperAndRepository(request, savedProduct, expectedResponse);

        // When
        ProductResponse actualResponse = productService.createProduct(request);

        // Then
        assertProductResponseEqualsRequest(actualResponse, request, PRODUCT_ID);
        verify(productRepository).save(Mockito.any(Product.class));
        verify(productMapper).mapToProductResponse(savedProduct);
    }


    @Test
    void shouldNotCreateInvalidProduct() {
        // Given - request with invalid data: null name, negative price, null category
        ProductRequest invalidRequest = ProductRequest.builder()
                .name(null)
                .description(PRODUCT_DESCRIPTION)
                .category(PRODUCT_CATEGORY)
                .price(PRODUCT_PRICE)
                .stockQuantity(STOCK_QUANTITY)
                .imageUrl(PRODUCT_URL)
                .build();

        // When & Then - service should reject invalid data with validation error
        assertThatThrownBy(() -> productService.createProduct(invalidRequest))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("Product name is required");

        // Verify that repository and mapper were never called
        verify(productRepository, never()).save(any(Product.class));
        verify(productMapper, never()).mapToProductResponse(any());
    }

    @Test
    void shouldNotCreateProductWithoutCategory() {
        ProductRequest invalidRequest = mockProductRequest();
        invalidRequest.setCategory(" ");

        assertThatThrownBy(() -> productService.createProduct(invalidRequest))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("Category is required");

        verifyNoInteractions(productRepository, productMapper);
    }

    @Test
    void shouldNotCreateProductWithNonPositivePrice() {
        ProductRequest invalidRequest = mockProductRequest();
        invalidRequest.setPrice(BigDecimal.ZERO);

        assertThatThrownBy(() -> productService.createProduct(invalidRequest))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("Price must be greater than 0");

        verifyNoInteractions(productRepository, productMapper);
    }

    @Test
    void shouldNotCreateProductWithNegativeStockQuantity() {
        ProductRequest invalidRequest = mockProductRequest();
        invalidRequest.setStockQuantity(-1);

        assertThatThrownBy(() -> productService.createProduct(invalidRequest))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("Stock quantity cannot be negative");

        verifyNoInteractions(productRepository, productMapper);
    }

    @Test
    void shouldUpdateProduct() {
        // Given
        Product mockedProduct = mockProduct();
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(mockedProduct));
        ProductRequest productRequest = mockProductRequest();
        Product savedProduct = mockSavedProduct(productRequest, PRODUCT_ID);
        ProductResponse expectedResponse = mockProductResponse(productRequest, PRODUCT_ID);
        mockMapperAndRepository(productRequest, savedProduct, expectedResponse);

        // When
        Optional<ProductResponse> actualResponse = productService.updateProduct(PRODUCT_ID, productRequest);

        // Then
        assertThat(actualResponse).isPresent();
        assertProductResponseEqualsRequest(actualResponse.get(), productRequest, PRODUCT_ID);
        verify(productRepository).findById(PRODUCT_ID);
        verify(productMapper).updateProductFromRequest(mockedProduct, productRequest);
        verify(productRepository).save(mockedProduct);
        verify(productMapper).mapToProductResponse(savedProduct);
    }

    @Test
    void shouldNotUpdateProductWhenItDoesNotExist() {
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.empty());

        Optional<ProductResponse> actualResponse = productService.updateProduct(PRODUCT_ID, mockProductRequest());

        assertThat(actualResponse).isEmpty();
        verify(productRepository).findById(PRODUCT_ID);
        verify(productRepository, never()).save(any(Product.class));
        verifyNoInteractions(productMapper);
    }

    @Test
    void shouldGetAllProducts() {
        Product firstProduct = mockProduct();
        Product secondProduct = mockProduct();
        secondProduct.setId(2L);
        secondProduct.setName("Another product");
        ProductResponse firstResponse = mockProductResponse(firstProduct);
        ProductResponse secondResponse = mockProductResponse(secondProduct);
        when(productRepository.findByIsActiveTrue()).thenReturn(List.of(firstProduct, secondProduct));
        when(productMapper.mapToProductResponse(firstProduct)).thenReturn(firstResponse);
        when(productMapper.mapToProductResponse(secondProduct)).thenReturn(secondResponse);

        List<ProductResponse> actualResponses = productService.getAllProducts();

        assertThat(actualResponses).containsExactly(firstResponse, secondResponse);
        verify(productRepository).findByIsActiveTrue();
        verify(productMapper).mapToProductResponse(firstProduct);
        verify(productMapper).mapToProductResponse(secondProduct);
    }

    @Test
    void shouldDeleteProduct() {
        Product product = mockProduct();
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));

        boolean deleted = productService.deleteProduct(PRODUCT_ID);

        assertThat(deleted).isTrue();
        assertThat(product.isActive()).isFalse();
        verify(productRepository).findById(PRODUCT_ID);
        verify(productRepository).save(product);
        verifyNoInteractions(productMapper);
    }

    @Test
    void shouldReturnFalseWhenDeletingProductThatDoesNotExist() {
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.empty());

        boolean deleted = productService.deleteProduct(PRODUCT_ID);

        assertThat(deleted).isFalse();
        verify(productRepository).findById(PRODUCT_ID);
        verify(productRepository, never()).save(any(Product.class));
        verifyNoInteractions(productMapper);
    }

    @Test
    void shouldSearchByKeyword() {
        Product product = mockProduct();
        ProductResponse expectedResponse = mockProductResponse(product);
        when(productRepository.searchByKeyword("dummy")).thenReturn(List.of(product));
        when(productMapper.mapToProductResponse(product)).thenReturn(expectedResponse);

        List<ProductResponse> actualResponses = productService.searchByKeyword("dummy");

        assertThat(actualResponses).containsExactly(expectedResponse);
        verify(productRepository).searchByKeyword("dummy");
        verify(productMapper).mapToProductResponse(product);
    }

    @Test
    void shouldHandleSearchByInvalidKeyword() {
        when(productRepository.searchByKeyword(null)).thenReturn(List.of());

        List<ProductResponse> actualResponses = productService.searchByKeyword(null);

        assertThat(actualResponses).isEmpty();
        verify(productRepository).searchByKeyword(null);
        verifyNoInteractions(productMapper);
    }

    @Test
    void shouldGetProductById() {
        Product product = mockProduct();
        ProductResponse expectedResponse = mockProductResponse(product);
        when(productRepository.findByIdAndIsActiveTrue(PRODUCT_ID)).thenReturn(Optional.of(product));
        when(productMapper.mapToProductResponse(product)).thenReturn(expectedResponse);

        Optional<ProductResponse> actualResponse = productService.getProductById(PRODUCT_ID);

        assertThat(actualResponse).contains(expectedResponse);
        verify(productRepository).findByIdAndIsActiveTrue(PRODUCT_ID);
        verify(productMapper).mapToProductResponse(product);
    }

    @Test
    void shouldReturnEmptyWhenProductByIdDoesNotExistOrIsInactive() {
        when(productRepository.findByIdAndIsActiveTrue(PRODUCT_ID)).thenReturn(Optional.empty());

        Optional<ProductResponse> actualResponse = productService.getProductById(PRODUCT_ID);

        assertThat(actualResponse).isEmpty();
        verify(productRepository).findByIdAndIsActiveTrue(PRODUCT_ID);
        verifyNoInteractions(productMapper);
    }


    private Product mockSavedProduct(ProductRequest request, Long id) {
        return Product.builder()
                .id(id)
                .name(request.getName())
                .description(request.getDescription())
                .category(request.getCategory())
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity())
                .imageUrl(request.getImageUrl())
                .isActive(true)
                .build();
    }

    private Product mockProduct() {
        return Product.builder()
                .id(PRODUCT_ID)
                .name(PRODUCT_NAME)
                .description(PRODUCT_DESCRIPTION)
                .category(PRODUCT_CATEGORY)
                .price(PRODUCT_PRICE)
                .stockQuantity(STOCK_QUANTITY)
                .imageUrl(PRODUCT_URL)
                .isActive(true)
                .build();
    }

    private ProductResponse mockProductResponse(ProductRequest request, Long id) {
        return ProductResponse.builder()
                .id(id)
                .name(request.getName())
                .description(request.getDescription())
                .category(request.getCategory())
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity())
                .imageUrl(request.getImageUrl())
                .isActive(true)
                .build();
    }

    private ProductResponse mockProductResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .category(product.getCategory())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .imageUrl(product.getImageUrl())
                .isActive(product.isActive())
                .build();
    }

    private void assertProductResponseEqualsRequest(ProductResponse response, ProductRequest request, Long expectedId) {
        assertThat(response).usingRecursiveComparison()
                .ignoringFields("id", "isActive")
                .isEqualTo(request);
        assertThat(response.getId()).isEqualTo(expectedId);
        assertThat(response.isActive()).isTrue();
    }

    private void mockMapperAndRepository(ProductRequest request, Product savedProduct, ProductResponse response) {
        doAnswer(invocation -> {
            Product prod = invocation.getArgument(0);
            prod.setName(request.getName());
            prod.setDescription(request.getDescription());
            prod.setCategory(request.getCategory());
            prod.setPrice(request.getPrice());
            prod.setStockQuantity(request.getStockQuantity());
            prod.setImageUrl(request.getImageUrl());
            return null;
        }).when(productMapper).updateProductFromRequest(Mockito.any(Product.class), Mockito.eq(request));
        when(productRepository.save(Mockito.any(Product.class))).thenReturn(savedProduct);
        when(productMapper.mapToProductResponse(savedProduct)).thenReturn(response);
    }

    private ProductRequest mockProductRequest() {
        return ProductRequest.builder()
                .name(PRODUCT_NAME)
                .description(PRODUCT_DESCRIPTION)
                .category(PRODUCT_CATEGORY)
                .price(PRODUCT_PRICE)
                .stockQuantity(STOCK_QUANTITY)
                .imageUrl(PRODUCT_URL)
                .build();
    }
}