package com.eshop.userservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.eshop.userservice.UserServiceApplication;
import com.eshop.userservice.dto.UserDTO;
import com.eshop.userservice.entity.Role;

//@SpringBootTest(classes = UserServiceApplication.class)
public class CustomerServiceTests {
	//@MockBean
	private CustomerService customerService;
	
	private static final String USERNAME = "User123";
	private static final String PASSWORD = "Password123!";

	//@Test
	void testSuccessfullyRegisterCustomer() {
		UserDTO userDTO = UserDTO.builder()
				.id(1L)
        		.username(USERNAME)
        		.password(PASSWORD)
        		.role(Role.CUSTOMER).build();
		
		when(customerService.registerCustomer(Mockito.any(UserDTO.class))).thenReturn(userDTO);
		
		UserDTO result = customerService.registerCustomer(userDTO);
		
		assertThat(result).isNotNull()
		.extracting(UserDTO::getId, UserDTO::getUsername, UserDTO::getRole)
		.containsExactly(1L, USERNAME, Role.CUSTOMER);
	}
	
}
