package com.eshop.storageservice.service.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
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
import com.eshop.storageservice.dto.UpdateProductDTO;
import com.eshop.storageservice.entity.Product;
import com.eshop.storageservice.exception.BusinessException;
import com.eshop.storageservice.exception.NotFoundException;
import com.eshop.storageservice.kafka.Producer;
import com.eshop.storageservice.mapper.ProductMapper;
import com.eshop.storageservice.repository.StorageRepository;
import com.eshop.storageservice.service.StorageService;
import com.eshop.storageservice.service.UserAuthentication;
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
	private final String PRODUCT_DESCRIPTION = "Product 1 Description";
	private final BigDecimal PRODUCT_PRICE = BigDecimal.valueOf(2.60);
	private final int PRODUCT_AVAILABILITY = 3;
	
	private final String PRODUCT2_NAME = "Product 2";
	private final String PRODUCT2_DESCRIPTION = "Product 2 Description";
	private final BigDecimal PRODUCT2_PRICE = BigDecimal.valueOf(1.80);
	private final int PRODUCT2_AVAILABILITY = 5;
	
	private static final MockHttpServletRequest request = new MockHttpServletRequest();
	
	@BeforeAll
	static void setup() {
		request.addHeader("Authorization", "token");
	}

	@Test
	void testSuccessfullyGetProducts() {
		Product product = createProduct(PRODUCT_NAME, PRODUCT_DESCRIPTION, PRODUCT_PRICE, PRODUCT_AVAILABILITY, null);
		Product product2 = createProduct(PRODUCT2_NAME, PRODUCT2_DESCRIPTION, PRODUCT2_PRICE, PRODUCT2_AVAILABILITY, null);
		product2.setId(2L);
		
		ProductDTO productDTO = buildProduct(PRODUCT_NAME, PRODUCT_DESCRIPTION, PRODUCT_PRICE, PRODUCT_AVAILABILITY);
		ProductDTO product2DTO = buildProduct(PRODUCT2_NAME, PRODUCT2_DESCRIPTION, PRODUCT2_PRICE, PRODUCT2_AVAILABILITY);
		product2DTO.setId(2L);
		
		Page<Product> products = new PageImpl<>(List.of(product, product2));
		Page<ProductDTO> productsDTO = new PageImpl<>(List.of(productDTO, product2DTO));
		
		when(storageRepository.findAll(any(Pageable.class))).thenReturn(products);
		when(productMapper.toDTOs(Mockito.<Page<Product>>any())).thenReturn(productsDTO);
		
		Page<ProductDTO> result = storageService.getProducts(PageRequest.of(0, 20));
		
		assertThat(result).isNotNull().isNotEmpty()
		.extracting(ProductDTO::getId, ProductDTO::getName, ProductDTO::getDescription, ProductDTO::getPrice, ProductDTO::getAvailability)
		.containsExactly(
				tuple(1L, PRODUCT_NAME, PRODUCT_DESCRIPTION, PRODUCT_PRICE, PRODUCT_AVAILABILITY),
				tuple(2L, PRODUCT2_NAME, PRODUCT2_DESCRIPTION, PRODUCT2_PRICE, PRODUCT2_AVAILABILITY));
	}
	
	@Test
	void testSuccessfullyGetProduct() {
		ProductDTO productDTO = buildProduct(PRODUCT_NAME, PRODUCT_DESCRIPTION, PRODUCT_PRICE, PRODUCT_AVAILABILITY);
		
		when(storageRepository.findById(anyLong())).thenReturn(
				Optional.of(createProduct(PRODUCT_NAME, PRODUCT_DESCRIPTION, PRODUCT_PRICE, PRODUCT_AVAILABILITY, null)));
		when(productMapper.toDTO(any(Product.class))).thenReturn(productDTO);
		
		ProductDTO result = storageService.getProduct(1L);
		
		assertThat(result).isNotNull()
		.extracting(ProductDTO::getId, ProductDTO::getName, ProductDTO::getDescription, ProductDTO::getPrice, ProductDTO::getAvailability)
		.containsExactly(1L, PRODUCT_NAME, PRODUCT_DESCRIPTION, PRODUCT_PRICE, PRODUCT_AVAILABILITY);
	}
	
	@Test
	void testFailGetProductWithInvalidId() {
		assertThrows(NotFoundException.class, () -> storageService.getProduct(500L));
	}
	
	 @Test
	 void testSuccessfullyAddProduct() throws IOException {
		 ProductDTO productDTO = buildProduct(PRODUCT_NAME, PRODUCT_DESCRIPTION, PRODUCT_PRICE, PRODUCT_AVAILABILITY);
		 
		 when(storageRepository.save(any(Product.class)))
		 	.thenReturn(createProduct(PRODUCT_NAME, PRODUCT_DESCRIPTION, PRODUCT_PRICE, PRODUCT_AVAILABILITY, null));
		 when(productMapper.toDTO(any(Product.class))).thenReturn(productDTO);
		 
		 ProductDTO result = storageService.addProduct(productDTO, null);
		 
		 verify(storageRepository).save(any(Product.class));
		 
		 assertThat(result).isNotNull()
			.extracting(ProductDTO::getId, ProductDTO::getName, ProductDTO::getPrice, ProductDTO::getAvailability)
			.containsExactly(1L, PRODUCT_NAME, PRODUCT_PRICE, PRODUCT_AVAILABILITY);
	 }
	 
	 @Test
	 void testFailAddProductWithAlreadyOccupiedName() {
		 ProductDTO productDTO = buildProduct(PRODUCT_NAME, PRODUCT_DESCRIPTION, PRODUCT_PRICE, PRODUCT_AVAILABILITY);
			
		 when(storageRepository.existsByName(anyString())).thenReturn(true);
			
		 assertThrows(BusinessException.class, () -> storageService.addProduct(productDTO, null));
	 }
	 
	 @Test
	 void testSuccessfullyUpdateProduct() throws IOException {
		 Product product = createProduct(PRODUCT_NAME, PRODUCT_DESCRIPTION, PRODUCT_PRICE, PRODUCT_AVAILABILITY, null);
		 
		 UpdateProductDTO updateProductDTO = UpdateProductDTO.builder()
					.price(PRODUCT2_PRICE)
					.availability(PRODUCT2_AVAILABILITY).build();
		 
		 Product updatedProduct = product;
		 updatedProduct.setPrice(PRODUCT2_PRICE);
		 updatedProduct.setAvailability(PRODUCT2_AVAILABILITY);
		 
		 ProductDTO mappedProductDTO = buildProduct(PRODUCT_NAME, PRODUCT_DESCRIPTION, PRODUCT2_PRICE, PRODUCT2_AVAILABILITY);
		 
		 when(storageRepository.findById(anyLong())).thenReturn(Optional.of(product));
		 when(productMapper.updateProduct(any(Product.class), any(UpdateProductDTO.class))).thenReturn(updatedProduct);
		 when(productMapper.toDTO(any(Product.class))).thenReturn(mappedProductDTO);
		 
		 ProductDTO result = storageService.updateProduct(1L, updateProductDTO, null);
		 
		 verify(storageRepository).save(any(Product.class));
		 
		 assertThat(result).isNotNull()
			.extracting(ProductDTO::getId, ProductDTO::getName, ProductDTO::getDescription, ProductDTO::getPrice, ProductDTO::getAvailability)
			.containsExactly(1L, PRODUCT_NAME, PRODUCT_DESCRIPTION, PRODUCT2_PRICE, PRODUCT2_AVAILABILITY);
	 }
	 
	 @Test
	 void testFailUpdateProductWithInvalidId() {
		 UpdateProductDTO updateProductDTO = UpdateProductDTO.builder()
					.price(PRODUCT2_PRICE)
					.availability(PRODUCT2_AVAILABILITY).build();
			
		 assertThrows(NotFoundException.class, () -> storageService.updateProduct(500L, updateProductDTO, null));
	 }
	 
	 @Test
	 void testFailUpdateProductWithAlreadyOccupiedName() {
		 Product product = createProduct(PRODUCT_NAME, PRODUCT_DESCRIPTION, PRODUCT_PRICE, PRODUCT_AVAILABILITY, null);
		 
		 UpdateProductDTO updateProductDTO = UpdateProductDTO.builder()
					.name(PRODUCT2_NAME).build();
		 
		 when(storageRepository.findById(anyLong())).thenReturn(Optional.of(product));
		 when(storageRepository.existsByName(anyString())).thenReturn(true);
			
		 assertThrows(BusinessException.class, () -> storageService.updateProduct(product.getId(), updateProductDTO, null));
	 }
	 
	 @Test
	 void testSuccessfullyOrderProduct() throws JsonProcessingException {
		 ProductDTO productDTO = buildProduct(PRODUCT_NAME, PRODUCT_DESCRIPTION, PRODUCT_PRICE, PRODUCT_AVAILABILITY);
		 
		 when(storageRepository.findById(anyLong()))
		 	.thenReturn(Optional.of(createProduct(PRODUCT_NAME, PRODUCT_DESCRIPTION, PRODUCT_PRICE, PRODUCT_AVAILABILITY, null)));
		 when(productMapper.toDTO(any(Product.class))).thenReturn(productDTO);
		 when(userAuthentication.getRequest()).thenReturn(request);
		 
		 storageService.orderProduct(1L, 3);
		 
		 verify(producer).sendMessage(any(ProductDTO.class), anyInt(), anyString());
	 }
	 
	 @Test
	 void testFailOrderProductWithInvalidId() {
		 assertThrows(NotFoundException.class, () -> storageService.orderProduct(500L, 3));
	 }
	 
	 @Test
	 void testFailOrderProductWithInvalidAmount() {
		 Product product = createProduct(PRODUCT_NAME, PRODUCT_DESCRIPTION, PRODUCT_PRICE, PRODUCT_AVAILABILITY, null);
		 
		 when(storageRepository.findById(anyLong())).thenReturn(Optional.of(product));
		 
		 assertThrows(BusinessException.class, () -> storageService.orderProduct(1L, -1));
	 }
	 
	 @Test
	 void testSuccessfullyUpdateAvailability() {
		 when(storageRepository.findById(anyLong())).thenReturn(
				 Optional.of(new Product(PRODUCT_NAME, PRODUCT_DESCRIPTION, PRODUCT_PRICE, PRODUCT_AVAILABILITY, null)));
		 
		 storageService.updateAvailability(1L, 2);
		 
		 verify(storageRepository).save(any(Product.class));
	 }
	 
	 @Test
	 void testFailUpdateAvailabilityWithInvalidId() {
		 assertThrows(NotFoundException.class, () -> storageService.updateAvailability(500L, 2));
	 }
	 
	 private ProductDTO buildProduct(String name, String description, BigDecimal price, int availability) {
		 return ProductDTO.builder()
					.id(1L)
					.name(name)
					.description(description)
					.price(price)
					.availability(availability).build();
	 }
	 
	 private Product createProduct(String name, String description, BigDecimal price, int availability, byte[] image) {
		 Product product = new Product();
		 product.setId(1L);
		 product.setName(name);
		 product.setDescription(description);
		 product.setPrice(price);
		 product.setAvailability(availability);
		 product.setImage(image);
		 return product;
	 }

}
