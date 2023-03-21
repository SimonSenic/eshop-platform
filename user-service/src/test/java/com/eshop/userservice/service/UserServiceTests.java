package com.eshop.userservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.eshop.userservice.UserServiceApplication;
import com.eshop.userservice.dto.UpdateUserDTO;
import com.eshop.userservice.dto.UserDTO;

@SpringBootTest(classes = UserServiceApplication.class)
public class UserServiceTests {
	@MockBean
	private UserService userService;
	
	private static final String USERNAME = "User123";
	private static final String PASSWORD = "Password123!";

	@Test
	void testSuccessfulLogin() {
		UserDTO userDTO = UserDTO.builder()
				.id(1L)
        		.username(USERNAME)
        		.password(PASSWORD).build();
		
		when(userService.login(Mockito.any(UserDTO.class))).thenReturn(userDTO);
		
		UserDTO result = userService.login(userDTO);
		
		assertThat(result).isNotNull();
		assertEquals(result.getUsername(), USERNAME);
	}
	
	@Test
	void testSuccessfulUpdate() { //TODO
		UserDTO userDTO = UserDTO.builder()
				.id(1L)
        		.username(USERNAME)
        		.password(PASSWORD).build();
		
		UpdateUserDTO updateUserDTO;
		
		when(userService.login(Mockito.any(UserDTO.class))).thenReturn(userDTO);
		
		UserDTO result = userService.login(userDTO);
		
		assertThat(result).isNotNull();
		assertEquals(result.getUsername(), USERNAME);
	}
	
}
