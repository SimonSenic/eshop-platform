package com.eshop.userservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.eshop.userservice.UserServiceApplication;
import com.eshop.userservice.dto.UserDTO;
import com.eshop.userservice.entity.Role;

//@SpringBootTest(classes = UserServiceApplication.class)
public class AdminServiceTests {
	//@MockBean
	private AdminService adminService;
	
	private static final String USERNAME = "User123";
	private static final String PASSWORD = "Password123!";
	private static final String EMAIL = "email@gmail.com";

	/*@Test
	void testSuccessfullyGetUsers() {
		UserDTO user1 = UserDTO.builder()
				.id(1L)
        		.username("User1")
        		.password("Password123!")
        		.email("email1@gmail.com").build();
		
		UserDTO user2 = UserDTO.builder()
				.id(2L)
        		.username("User2")
        		.password("Password123!")
        		.email("email2@gmail.com").build();
		
		List<UserDTO> users = new ArrayList<>();
		users.add(user1);
		users.add(user2);
		
		when(adminService.getUsers()).thenReturn(users);
		
		List<UserDTO> result = adminService.getUsers();
		
		assertThat(result).isNotNull().isNotEmpty();
		assertEquals(result.get(0).getUsername(), user1.getUsername());
		assertEquals(result.get(1).getEmail(), user2.getEmail());
	}
	
	@Test
	void testSuccessfullyGetUser() {
		UserDTO user = UserDTO.builder()
				.id(1L)
        		.username(USERNAME)
        		.password(PASSWORD)
        		.email(EMAIL).build();
		
		when(adminService.getUser(Mockito.anyString())).thenReturn(user);
		
		UserDTO result = adminService.getUser(USERNAME);
		
		assertThat(result).isNotNull()
		.extracting(UserDTO::getId, UserDTO::getUsername, UserDTO::getEmail)
		.containsExactly(1L, USERNAME, EMAIL);
	}*/
	
	//@Test
	void testSuccessfullyCreateAdmin() {
		UserDTO userDTO = UserDTO.builder()
				.id(1L)
        		.username(USERNAME)
        		.password(PASSWORD)
        		.role(Role.ADMIN).build();
		
		when(adminService.createAdmin(Mockito.any(UserDTO.class))).thenReturn(userDTO);
		
		UserDTO result = adminService.createAdmin(userDTO);
		
		assertThat(result).isNotNull()
		.extracting(UserDTO::getId, UserDTO::getUsername, UserDTO::getRole)
		.containsExactly(1L, USERNAME, Role.ADMIN);
	}

}
