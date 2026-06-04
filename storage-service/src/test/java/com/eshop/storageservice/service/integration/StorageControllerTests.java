package com.eshop.storageservice.service.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.web.bind.MethodArgumentNotValidException;

import com.eshop.storageservice.dto.ProductDTO;
import com.eshop.storageservice.dto.UpdateProductDTO;
import com.eshop.storageservice.entity.Product;
import com.eshop.storageservice.exception.BusinessException;
import com.eshop.storageservice.exception.NotFoundException;
import com.eshop.storageservice.repository.StorageRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Import({ JwtTestComponent.class })
public class StorageControllerTests extends AbstractIntegrationTests {
	
	@Autowired
    private MockMvc mockMvc;
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
    
	@Autowired
    private StorageRepository storageRepository;
	
	@Autowired
	private JwtTestComponent jwtTestComponent;
    
	@Autowired
    private ObjectMapper objectMapper;
	
	private final String PRODUCT_NAME = "Product 1";
	private final String PRODUCT_DESCRIPTION = "Product 1 Description";
	private final BigDecimal PRODUCT_PRICE = BigDecimal.valueOf(2.60);
	private final int PRODUCT_AVAILABILITY = 3;
	private final byte[] PRODUCT_IMAGE = new byte[] {1, 2, 3, 4, 5};
	
	private final String PRODUCT2_NAME = "Product 2";
	private final String PRODUCT2_DESCRIPTION = "Product 2 Description";
	private final BigDecimal PRODUCT2_PRICE = BigDecimal.valueOf(1.80);
	private final int PRODUCT2_AVAILABILITY = 5;
	private final byte[] PRODUCT2_IMAGE = new byte[] {6, 7, 8, 9, 10};
	
	private String customerUserJwt;
	private String adminUserJwt;
	
	@BeforeEach
	void setup() {
	    jdbcTemplate.execute("TRUNCATE TABLE products RESTART IDENTITY CASCADE;");
	    
		customerUserJwt = jwtTestComponent.generateCustomerToken();
		adminUserJwt = jwtTestComponent.generateAdminToken();
	}
	
	@Test
	void testSuccessfullyGetProducts() throws Exception {
		storageRepository.save(createProduct(PRODUCT_NAME, PRODUCT_DESCRIPTION, PRODUCT_PRICE, PRODUCT_AVAILABILITY, PRODUCT_IMAGE));
		storageRepository.save(createProduct(PRODUCT2_NAME, PRODUCT2_DESCRIPTION, PRODUCT2_PRICE, PRODUCT2_AVAILABILITY, PRODUCT2_IMAGE));
		
		MvcResult mvcResult = mockMvc.perform(get("/storage-service/products")
				.param("page", "0").param("size", "20"))
                .andExpect(status().isOk()).andReturn();
		
		JsonNode root = objectMapper.readTree(mvcResult.getResponse().getContentAsString());
		List<ProductDTO> resultDTO = objectMapper.readValue(root.get("content").toString(), new TypeReference<List<ProductDTO>>() {});
		
		assertThat(resultDTO).isNotNull().isNotEmpty()
		.extracting(ProductDTO::getId, ProductDTO::getName, ProductDTO::getDescription, ProductDTO::getImage)
		.containsExactly(
				tuple(1L, PRODUCT_NAME, PRODUCT_DESCRIPTION, PRODUCT_IMAGE),
				tuple(2L, PRODUCT2_NAME, PRODUCT2_DESCRIPTION, PRODUCT2_IMAGE));
	}
	
	@Test
	void testSuccessfullyGetProduct() throws Exception {
		Product product = storageRepository.save(createProduct(PRODUCT_NAME, PRODUCT_DESCRIPTION, PRODUCT_PRICE, PRODUCT_AVAILABILITY, PRODUCT_IMAGE));
		
		MvcResult mvcResult = mockMvc.perform(get("/storage-service/products/{id}", product.getId()))
                .andExpect(status().isOk()).andReturn();
		
		ProductDTO resultDTO = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ProductDTO.class);
		
		assertThat(resultDTO).isNotNull()
		.extracting(ProductDTO::getId, ProductDTO::getName, ProductDTO::getDescription, ProductDTO::getImage)
		.containsExactly(1L, PRODUCT_NAME, PRODUCT_DESCRIPTION, PRODUCT_IMAGE);
		
		assertThat(resultDTO.getPrice())
		.usingComparator(BigDecimal::compareTo)
		.isEqualTo(PRODUCT_PRICE);
	}
	
	@Test
	void testFailGetProductWithInvalidId() throws Exception {
		MvcResult mvcResult = mockMvc.perform(get("/storage-service/products/{id}", 500L))
                .andExpect(status().isNotFound()).andReturn();
		
		assertThat(mvcResult.getResolvedException()).isNotNull()
		.isInstanceOf(NotFoundException.class);
	}
	
	@Test
	void testSuccessfullyAddProduct() throws Exception {
		ProductDTO productDTO = buildProduct(PRODUCT_NAME, PRODUCT_DESCRIPTION, PRODUCT_PRICE, PRODUCT_AVAILABILITY, null);
		
		MockMultipartFile image = new MockMultipartFile("image", "", 
				MediaType.IMAGE_PNG_VALUE, PRODUCT_IMAGE);
		
		MockMultipartFile product = new MockMultipartFile("productDTO", "", 
				MediaType.APPLICATION_JSON_VALUE, objectMapper.writeValueAsBytes(productDTO));
		
		MvcResult mvcResult = mockMvc.perform(multipart(POST, "/storage-service/products/add")
				.file(product).file(image)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " +adminUserJwt))
                .andExpect(status().isCreated()).andReturn();
		
		ProductDTO resultDTO = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ProductDTO.class);

        assertThat(resultDTO).isNotNull()
		.extracting(ProductDTO::getId, ProductDTO::getName, ProductDTO::getDescription, ProductDTO::getImage)
		.containsExactly(1L, PRODUCT_NAME, PRODUCT_DESCRIPTION, PRODUCT_IMAGE);

        Product dbResult = storageRepository.findById(resultDTO.getId())
        		.orElseThrow(() -> new AssertionError("Product not found"));
        
        assertThat(dbResult)
		.extracting(Product::getId, Product::getName, Product::getDescription, Product::getImage)
		.containsExactly(1L, PRODUCT_NAME, PRODUCT_DESCRIPTION, PRODUCT_IMAGE);
	}
	
	@Test
	void testFailAddProductWithInvalidPrice() throws Exception {
		ProductDTO productDTO = buildProduct(PRODUCT_NAME, PRODUCT_DESCRIPTION, PRODUCT_PRICE, PRODUCT_AVAILABILITY, null);
		productDTO.setPrice(BigDecimal.valueOf(-1));
		
		MockMultipartFile product = new MockMultipartFile("productDTO", "", 
				MediaType.APPLICATION_JSON_VALUE, objectMapper.writeValueAsBytes(productDTO));

		MvcResult mvcResult = mockMvc.perform(multipart(POST, "/storage-service/products/add")
				.file(product)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " +adminUserJwt))
                .andExpect(status().isBadRequest()).andReturn();
		
		assertThat(mvcResult.getResolvedException()).isNotNull()
		.isInstanceOf(MethodArgumentNotValidException.class);
	}
	
	@Test
	void testFailAddProductWithAlreadyOccupyingName() throws Exception {
		Product product = storageRepository.save(createProduct(PRODUCT_NAME, PRODUCT_DESCRIPTION, PRODUCT_PRICE, PRODUCT_AVAILABILITY, null));
		ProductDTO newProductDTO = buildProduct(PRODUCT2_NAME, PRODUCT2_DESCRIPTION, PRODUCT2_PRICE, PRODUCT2_AVAILABILITY, null);
		newProductDTO.setName(product.getName());
		
		MockMultipartFile newProduct = new MockMultipartFile("productDTO", "", 
				MediaType.APPLICATION_JSON_VALUE, objectMapper.writeValueAsBytes(newProductDTO));

		MvcResult mvcResult = mockMvc.perform(multipart(POST, "/storage-service/products/add")
				.file(newProduct)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " +adminUserJwt))
                .andExpect(status().isBadRequest()).andReturn();
		
		assertThat(mvcResult.getResolvedException()).isNotNull()
		.isInstanceOf(BusinessException.class);
	}
	
	@Test
	void testFailAddProductWithOversizedImage() throws Exception {
		ProductDTO productDTO = buildProduct(PRODUCT_NAME, PRODUCT_DESCRIPTION, PRODUCT_PRICE, PRODUCT_AVAILABILITY, null);
		
		MockMultipartFile image = new MockMultipartFile("image", "", 
				MediaType.IMAGE_PNG_VALUE, new byte[12 * 1024 * 1024]);
		
		MockMultipartFile product = new MockMultipartFile("productDTO", "", 
				MediaType.APPLICATION_JSON_VALUE, objectMapper.writeValueAsBytes(productDTO));

		MvcResult mvcResult = mockMvc.perform(multipart(POST, "/storage-service/products/add")
				.file(product).file(image)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " +adminUserJwt))
                .andExpect(status().isBadRequest()).andReturn();
		
		assertThat(mvcResult.getResolvedException()).isNotNull()
		.isInstanceOf(BusinessException.class);
	}
	
	@Test
	void testFailAddProductAsCustomer() throws Exception {
		ProductDTO productDTO = buildProduct(PRODUCT_NAME, PRODUCT_DESCRIPTION, PRODUCT_PRICE, PRODUCT_AVAILABILITY, null);
		
		MockMultipartFile product = new MockMultipartFile("productDTO", "", 
				MediaType.APPLICATION_JSON_VALUE, objectMapper.writeValueAsBytes(productDTO));

		mockMvc.perform(multipart(POST, "/storage-service/products/add")
				.file(product)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " +customerUserJwt))
				.andExpect(status().isForbidden());
	}
	
	@Test
	void testSuccessfullyUpdateProduct() throws Exception {
		Product product = storageRepository.save(createProduct(PRODUCT_NAME, PRODUCT_DESCRIPTION, PRODUCT_PRICE, PRODUCT_AVAILABILITY, PRODUCT_IMAGE));
		UpdateProductDTO updateProductDTO = buildUpdateProduct(PRODUCT2_NAME, PRODUCT2_DESCRIPTION, null, null);
		
		MockMultipartFile image = new MockMultipartFile("image", "", 
				MediaType.IMAGE_PNG_VALUE, PRODUCT2_IMAGE);
		
		MockMultipartFile updateProduct = new MockMultipartFile("updateProductDTO", "", 
				MediaType.APPLICATION_JSON_VALUE, objectMapper.writeValueAsBytes(updateProductDTO));
		
		MvcResult mvcResult = mockMvc.perform(multipart(PATCH, "/storage-service/products/{id}/update", product.getId())
				.file(updateProduct).file(image)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " +adminUserJwt))
                .andExpect(status().isOk()).andReturn();
		
		ProductDTO resultDTO = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ProductDTO.class);

        assertThat(resultDTO).isNotNull()
		.extracting(ProductDTO::getId, ProductDTO::getName, ProductDTO::getDescription, ProductDTO::getImage)
		.containsExactly(1L, PRODUCT2_NAME, PRODUCT2_DESCRIPTION, PRODUCT2_IMAGE);

        Product dbResult = storageRepository.findById(resultDTO.getId())
        		.orElseThrow(() -> new AssertionError("Product not found"));
        
        assertThat(dbResult)
		.extracting(Product::getId, Product::getName, Product::getDescription, Product::getImage)
		.containsExactly(1L, PRODUCT2_NAME, PRODUCT2_DESCRIPTION, PRODUCT2_IMAGE);
	}
	
	@Test
	void testFailUpdateProductWithInvalidPrice() throws Exception {
		Product product = storageRepository.save(createProduct(PRODUCT_NAME, PRODUCT_DESCRIPTION, PRODUCT_PRICE, PRODUCT_AVAILABILITY, PRODUCT_IMAGE));
		UpdateProductDTO updateProductDTO = buildUpdateProduct(PRODUCT2_NAME, PRODUCT2_DESCRIPTION, BigDecimal.valueOf(-1), null);
		
		MockMultipartFile updateProduct = new MockMultipartFile("updateProductDTO", "", 
				MediaType.APPLICATION_JSON_VALUE, objectMapper.writeValueAsBytes(updateProductDTO));

		MvcResult mvcResult = mockMvc.perform(multipart(PATCH, "/storage-service/products/{id}/update", product.getId())
				.file(updateProduct)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " +adminUserJwt))
                .andExpect(status().isBadRequest()).andReturn();
		
		assertThat(mvcResult.getResolvedException()).isNotNull()
		.isInstanceOf(MethodArgumentNotValidException.class);
	}
	
	@Test
	void testFailUpdateProductWithInvalidId() throws Exception {
		UpdateProductDTO updateProductDTO = buildUpdateProduct(PRODUCT2_NAME, PRODUCT2_DESCRIPTION, null, null);
		
		MockMultipartFile updateProduct = new MockMultipartFile("updateProductDTO", "", 
				MediaType.APPLICATION_JSON_VALUE, objectMapper.writeValueAsBytes(updateProductDTO));

		MvcResult mvcResult = mockMvc.perform(multipart(PATCH, "/storage-service/products/{id}/update", 500L)
				.file(updateProduct)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " +adminUserJwt))
                .andExpect(status().isNotFound()).andReturn();
		
		assertThat(mvcResult.getResolvedException()).isNotNull()
		.isInstanceOf(NotFoundException.class);
	}
	
	@Test
	void testFailUpdateProductWithAlreadyOccupyingName() throws Exception {
		Product product = storageRepository.save(createProduct(PRODUCT_NAME, PRODUCT_DESCRIPTION, PRODUCT_PRICE, PRODUCT_AVAILABILITY, PRODUCT_IMAGE));
		Product product2 = storageRepository.save(createProduct(PRODUCT2_NAME, PRODUCT2_DESCRIPTION, PRODUCT2_PRICE, PRODUCT2_AVAILABILITY, PRODUCT2_IMAGE));
		UpdateProductDTO updateProduct2DTO = buildUpdateProduct(product.getName(), product.getDescription(), null, null);
		
		MockMultipartFile updateProduct2 = new MockMultipartFile("updateProductDTO", "", 
				MediaType.APPLICATION_JSON_VALUE, objectMapper.writeValueAsBytes(updateProduct2DTO));

		MvcResult mvcResult = mockMvc.perform(multipart(PATCH, "/storage-service/products/{id}/update", product2.getId())
				.file(updateProduct2)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " +adminUserJwt))
                .andExpect(status().isBadRequest()).andReturn();
		
		assertThat(mvcResult.getResolvedException()).isNotNull()
		.isInstanceOf(BusinessException.class);
	}
	
	@Test
	void testFailUpdateProductWithOversizedImage() throws Exception {
		Product product = storageRepository.save(createProduct(PRODUCT_NAME, PRODUCT_DESCRIPTION, PRODUCT_PRICE, PRODUCT_AVAILABILITY, PRODUCT_IMAGE));
		UpdateProductDTO updateProductDTO = UpdateProductDTO.builder().build();
		
		MockMultipartFile image = new MockMultipartFile("image", "", 
				MediaType.IMAGE_PNG_VALUE, new byte[12 * 1024 * 1024]);
		
		MockMultipartFile updateProduct = new MockMultipartFile("updateProductDTO", "", 
				MediaType.APPLICATION_JSON_VALUE, objectMapper.writeValueAsBytes(updateProductDTO));

		MvcResult mvcResult = mockMvc.perform(multipart(PATCH, "/storage-service/products/{id}/update", product.getId())
				.file(updateProduct).file(image)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " +adminUserJwt))
                .andExpect(status().isBadRequest()).andReturn();
		
		assertThat(mvcResult.getResolvedException()).isNotNull()
		.isInstanceOf(BusinessException.class);
	}
	
	@Test
	void testSuccessfullyOrderProduct() throws Exception {
		Product product = storageRepository.save(createProduct(PRODUCT_NAME, PRODUCT_DESCRIPTION, PRODUCT_PRICE, PRODUCT_AVAILABILITY, PRODUCT_IMAGE));
		
		mockMvc.perform(post("/storage-service/products/{id}/order", product.getId())
				.header(HttpHeaders.AUTHORIZATION, "Bearer " +customerUserJwt)
                .param("amount", "2"))
                .andExpect(status().isOk());
	}
	
	@Test
	void testFailOrderProductWithInvalidId() throws Exception {
		mockMvc.perform(post("/storage-service/products/{id}/order", 500L)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " +customerUserJwt))
                .andExpect(status().isNotFound());
	}
	
	@Test
	void testFailOrderProductWithInvalidAmount() throws Exception {
		Product product = storageRepository.save(createProduct(PRODUCT_NAME, PRODUCT_DESCRIPTION, PRODUCT_PRICE, PRODUCT_AVAILABILITY, PRODUCT_IMAGE));
		
		mockMvc.perform(post("/storage-service/products/{id}/order", product.getId())
				.header(HttpHeaders.AUTHORIZATION, "Bearer " +customerUserJwt)
                .param("amount", "-1"))
                .andExpect(status().isBadRequest());
	}
	
	@Test
	void testFailOrderProductWithInvalidJwt() throws Exception {
		Product product = storageRepository.save(createProduct(PRODUCT_NAME, PRODUCT_DESCRIPTION, PRODUCT_PRICE, PRODUCT_AVAILABILITY, PRODUCT_IMAGE));
		customerUserJwt = jwtTestComponent.generateExpiredCustomerToken();
		
		mockMvc.perform(post("/storage-service/products/{id}/order", product.getId())
				.header(HttpHeaders.AUTHORIZATION, "Bearer " +customerUserJwt))
                .andExpect(status().isUnauthorized());
	}
	
	@Test
	void testSuccessfullyUpdateAvailability() throws Exception {
		Product product = storageRepository.save(createProduct(PRODUCT_NAME, PRODUCT_DESCRIPTION, PRODUCT_PRICE, PRODUCT_AVAILABILITY, PRODUCT_IMAGE));
		
		mockMvc.perform(put("/storage-service/products/{id}/update-availability", product.getId())
				.with(setRemoteAddr())
                .param("increase", "5"))
                .andExpect(status().isOk());
		
		Product dbResult = storageRepository.findById(product.getId())
        		.orElseThrow(() -> new AssertionError("Product not found"));
        
        assertThat(dbResult)
		.extracting(Product::getId, Product::getAvailability)
		.containsExactly(1L, PRODUCT_AVAILABILITY + 5);
	}
	
	@Test
	void testFailUpdateAvailabilityWithInvalidId() throws Exception {
		mockMvc.perform(put("/storage-service/products/{id}/update-availability", 500L)
				.with(setRemoteAddr())
                .param("increase", "5"))
                .andExpect(status().isNotFound());
	}
	
	@Test
	void testFailUpdateAvailabilityWithInvalidAddress() throws Exception {
		Product product = storageRepository.save(createProduct(PRODUCT_NAME, PRODUCT_DESCRIPTION, PRODUCT_PRICE, PRODUCT_AVAILABILITY, PRODUCT_IMAGE));
		
		mockMvc.perform(put("/storage-service/products/{id}/update-availability", product.getId())
                .param("increase", "5"))
                .andExpect(status().isForbidden());
	}
	
	private Product createProduct(String name, String description, BigDecimal price, int availability, byte[] image) {
		Product product = new Product();
		product.setName(name);
		product.setDescription(description);
		product.setPrice(price);
		product.setAvailability(availability);
		product.setImage(image);
		return product;
	}
	
	private ProductDTO buildProduct(String name, String description, BigDecimal price, int availability, byte[] image) {
		return ProductDTO.builder()
				.name(name)
				.description(description)
				.price(price)
				.availability(availability)
				.image(image).build();
	}
	
	private UpdateProductDTO buildUpdateProduct(String name, String description, BigDecimal price, Integer availability) {
		return UpdateProductDTO.builder()
				.name(name)
				.description(description)
				.price(price)
				.availability(availability).build();
	}
	
	private RequestPostProcessor setRemoteAddr() {
		return req -> {
			req.setRemoteAddr("192.168.100.186");
			return req;
		};
	}

}
