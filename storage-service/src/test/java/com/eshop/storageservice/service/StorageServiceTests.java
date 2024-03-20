package com.eshop.storageservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockHttpServletRequest;

import com.eshop.storageservice.dto.ProductDTO;
import com.eshop.storageservice.entity.Product;
import com.eshop.storageservice.kafka.Producer;
import com.eshop.storageservice.mapper.ProductMapper;
import com.eshop.storageservice.repository.StorageRepository;
import com.fasterxml.jackson.core.JsonProcessingException;

@SpringBootTest
class StorageServiceTests {
	
	@InjectMocks
	private StorageService storageService;
	
	@Mock
	private StorageRepository storageRepository;
	
	@Mock
	private ProductMapper productMapper;
	
	@Mock
	private UserAuthentication userAuthentication;
	
	@Mock
	private Producer producer;
	
	private final String PRODUCT_NAME = "Product 1";
	private final BigDecimal PRODUCT_PRICE = BigDecimal.valueOf(2.60);
	private final int PRODUCT_AVAILABILITY = 3;
	
	private final String PRODUCT2_NAME = "Product 2";
	private final BigDecimal PRODUCT2_PRICE = BigDecimal.valueOf(1.80);
	private final int PRODUCT2_AVAILABILITY = 5;
	
	private static final MockHttpServletRequest request = new MockHttpServletRequest();
	
	@BeforeAll
	static void setup() {
		request.addHeader("Authorization", "token");
	}

	@Test
	void testSuccessfullyGetProducts() {
		Product product = new Product(PRODUCT_NAME, PRODUCT_PRICE, PRODUCT_AVAILABILITY);
		Product product2 = new Product(PRODUCT2_NAME, PRODUCT2_PRICE, PRODUCT2_AVAILABILITY);
		
		ProductDTO productDTO = ProductDTO.builder()
				.id(1L)
				.name(PRODUCT_NAME)
				.price(PRODUCT_PRICE)
				.availability(PRODUCT_AVAILABILITY).build();
		
		ProductDTO product2DTO = ProductDTO.builder()
				.id(2L)
				.name(PRODUCT2_NAME)
				.price(PRODUCT2_PRICE)
				.availability(PRODUCT2_AVAILABILITY).build();
		
		Page<Product> products = new PageImpl<>(List.of(product, product2));
		Page<ProductDTO> productsDTO = new PageImpl<>(List.of(productDTO, product2DTO));
		
		when(storageRepository.findAll(any(Pageable.class))).thenReturn(products);
		when(productMapper.toDTOs(Mockito.<Page<Product>>any())).thenReturn(productsDTO);
		
		Page<ProductDTO> result = storageService.getProducts(PageRequest.of(0, 20));
		
		assertThat(result).isNotNull().isNotEmpty()
		.extracting(ProductDTO::getId, ProductDTO::getName, ProductDTO::getPrice, ProductDTO::getAvailability)
		.containsExactly(
				tuple(1L, PRODUCT_NAME, PRODUCT_PRICE, PRODUCT_AVAILABILITY),
				tuple(2L, PRODUCT2_NAME, PRODUCT2_PRICE, PRODUCT2_AVAILABILITY));
	}
	
	@Test
	void testSuccessfullyGetProduct() {
		ProductDTO productDTO = ProductDTO.builder()
				.id(1L)
				.name(PRODUCT_NAME)
				.price(PRODUCT_PRICE)
				.availability(PRODUCT_AVAILABILITY).build();
		
		when(storageRepository.findById(anyLong())).thenReturn(
				Optional.of(new Product(PRODUCT_NAME, PRODUCT_PRICE, PRODUCT_AVAILABILITY)));
		when(productMapper.toDTO(any(Product.class))).thenReturn(productDTO);
		
		ProductDTO result = storageService.getProduct(1L);
		
		assertThat(result).isNotNull()
		.extracting(ProductDTO::getId, ProductDTO::getName, ProductDTO::getPrice, ProductDTO::getAvailability)
		.containsExactly(1L, PRODUCT_NAME, PRODUCT_PRICE, PRODUCT_AVAILABILITY);
	}
	
	 @Test
	 void testSuccessfullyAddProduct() {
		 ProductDTO productDTO = ProductDTO.builder()
					.id(1L)
					.name(PRODUCT_NAME)
					.price(PRODUCT_PRICE)
					.availability(PRODUCT_AVAILABILITY).build();
		 
		 when(storageRepository.save(any(Product.class))).thenReturn(new Product(PRODUCT_NAME, PRODUCT_PRICE, PRODUCT_AVAILABILITY));
		 when(productMapper.toDTO(any(Product.class))).thenReturn(productDTO);
		 
		 ProductDTO result = storageService.addProduct(productDTO);
		 
		 verify(storageRepository).save(any(Product.class));
		 
		 assertThat(result).isNotNull()
			.extracting(ProductDTO::getId, ProductDTO::getName, ProductDTO::getPrice, ProductDTO::getAvailability)
			.containsExactly(1L, PRODUCT_NAME, PRODUCT_PRICE, PRODUCT_AVAILABILITY);
	 }
	 
	 @Test
	 void testSuccessfullyUpdateProduct() {
		 Product product = new Product(PRODUCT_NAME, PRODUCT_PRICE, PRODUCT_AVAILABILITY);
		 
		 ProductDTO productDTO = ProductDTO.builder()
					.id(1L)
					.price(PRODUCT2_PRICE)
					.availability(PRODUCT2_AVAILABILITY).build();
		 
		 Product updatedProduct = product;
		 updatedProduct.setPrice(PRODUCT2_PRICE);
		 updatedProduct.setAvailability(PRODUCT2_AVAILABILITY);
		 
		 ProductDTO mappedProductDTO = ProductDTO.builder()
					.id(1L)
					.name(PRODUCT_NAME)
					.price(PRODUCT2_PRICE)
					.availability(PRODUCT2_AVAILABILITY).build();
		 
		 when(storageRepository.findById(anyLong())).thenReturn(Optional.of(product));
		 when(productMapper.updateProduct(any(Product.class), any(ProductDTO.class))).thenReturn(updatedProduct);
		 when(productMapper.toDTO(any(Product.class))).thenReturn(mappedProductDTO);
		 
		 ProductDTO result = storageService.updateProduct(1L, productDTO);
		 
		 verify(storageRepository).save(any(Product.class));
		 
		 assertThat(result).isNotNull()
			.extracting(ProductDTO::getId, ProductDTO::getName, ProductDTO::getPrice, ProductDTO::getAvailability)
			.containsExactly(1L, PRODUCT_NAME, PRODUCT2_PRICE, PRODUCT2_AVAILABILITY);
	 }
	 
	 @Test
	 void testSuccessfullyOrderProduct() throws JsonProcessingException {
		 ProductDTO productDTO = ProductDTO.builder()
					.id(1L)
					.name(PRODUCT_NAME)
					.price(PRODUCT_PRICE)
					.availability(PRODUCT_AVAILABILITY).build();
		 
		 when(storageRepository.findById(anyLong())).thenReturn(Optional.of(new Product(PRODUCT_NAME, PRODUCT_PRICE, PRODUCT_AVAILABILITY)));
		 when(productMapper.toDTO(any(Product.class))).thenReturn(productDTO);
		 when(userAuthentication.getRequest()).thenReturn(request);
		 
		 storageService.orderProduct(1L, 3);
		 
		 verify(producer).sendMessage(any(ProductDTO.class), anyInt(), anyString());
	 }
	 
	 @Test
	 void testSuccessfullyUpdateAvailability() {
		 when(storageRepository.findById(anyLong())).thenReturn(
				 Optional.of(new Product(PRODUCT_NAME, PRODUCT_PRICE, PRODUCT_AVAILABILITY)));
		 
		 storageService.updateAvailability(1L, 2);
		 
		 verify(storageRepository).save(any(Product.class));
	 }

}
