package com.eshop.orderservice.service.integration;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import com.eshop.orderservice.repository.OrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Import({ JwtTestComponent.class })
public class OrderControllerTests extends AbstractIntegrationTests {
	
	@Autowired
    private MockMvc mockMvc;
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
    
	@Autowired
    private OrderRepository orderRepository;
	
	@Autowired
	private JwtTestComponent jwtTestComponent;
    
	@Autowired
    private ObjectMapper objectMapper;
    
	@Autowired
    private PasswordEncoder passwordEncoder;
	
	@Autowired
	private Environment environment;
	
	private String customerUserJwt;
	private String adminUserJwt;
	
	@BeforeEach
	void setup() {
	    jdbcTemplate.execute("TRUNCATE TABLE storage RESTART IDENTITY CASCADE;");
	    
		customerUserJwt = jwtTestComponent.generateCustomerToken();
		adminUserJwt = jwtTestComponent.generateAdminToken();
	}
	
	
	
	

}
