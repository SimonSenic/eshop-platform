package com.eshop.userservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.eshop.userservice.dto.UpdateUserDTO;
import com.eshop.userservice.dto.UserDTO;
import com.eshop.userservice.entity.Role;
import com.eshop.userservice.entity.User;
import com.eshop.userservice.mapper.UserMapper;
import com.eshop.userservice.repository.UserRepository;

@SpringBootTest
class UserServiceTests {
	
	@InjectMocks
	private UserService userService;
	
	@Mock
	private UserRepository userRepository;
	
	@Mock
	private UserMapper userMapper;
	
	@Mock
	private UserAuthentication userAuthentication;
	
	@Mock
	private PasswordEncoder passwordEncoder;
	
	@Mock
	private Environment environment;
	
	private final String USERNAME = "User123";
	private final String PASSWORD = "Password123!";
	private final String EMAIL = "user@gmail.com";
	private final String FIRSTNAME = "Firstname";
	private final String LASTNAME = "Lastname";
	private final String ADDRESS = "Address 123";
	
	private final String NEW_USERNAME = "NewUsername";
	private final String NEW_EMAIL = "new.email@gmail.com";
	
	private final Authentication auth = new UsernamePasswordAuthenticationToken(USERNAME, null, List.of(new SimpleGrantedAuthority(Role.CUSTOMER.name())));

	@Test
	void testSuccessfulLogin() {
		UserDTO userDTO = UserDTO.builder()
				.username(USERNAME)
				.password(PASSWORD).build();
		
		UserDTO mappedUserDTO = userDTO;
		mappedUserDTO.setId(1L);
		mappedUserDTO.setRole(Role.CUSTOMER);
		mappedUserDTO.setActive(true);
		
		when(userRepository.findByUsername(anyString())).thenReturn(
				Optional.of(new User(USERNAME, PASSWORD, EMAIL, FIRSTNAME, LASTNAME, ADDRESS, Role.CUSTOMER, true)));
		when(userMapper.toDTO(any(User.class))).thenReturn(mappedUserDTO);
		
		UserDTO result = userService.login(userDTO);
		
		assertThat(result).isNotNull()
		.extracting(UserDTO::getId, UserDTO::getUsername, UserDTO::getRole, UserDTO::getActive)
		.containsExactly(1L, USERNAME, Role.CUSTOMER, true);
	}
	
	@Test
	void testSuccessfulUpdate() {
		User user = new User(USERNAME, PASSWORD, EMAIL, FIRSTNAME, LASTNAME, ADDRESS, Role.CUSTOMER, true);
		
		UpdateUserDTO updateUserDTO = UpdateUserDTO.builder()
				.username(NEW_USERNAME)
				.password(PASSWORD)
				.email(NEW_EMAIL).build();
		
		User updatedUser = user;
		updatedUser.setId(1L);
		updatedUser.setUsername(NEW_USERNAME);
		updatedUser.setEmail(NEW_EMAIL);
		
		UserDTO mappedUserDTO = UserDTO.builder()
				.id(1L)
				.username(NEW_USERNAME)
				.email(NEW_EMAIL)
				.role(Role.CUSTOMER)
				.active(true).build();
		
		when(userAuthentication.getAuthentication()).thenReturn(auth);
		when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));
		when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
		when(userMapper.updateUser(any(User.class), any(UpdateUserDTO.class))).thenReturn(updatedUser);
		when(userMapper.toDTO(any(User.class))).thenReturn(mappedUserDTO);
		
		UserDTO result = userService.updateUser(updateUserDTO);
		
		verify(userRepository).save(any(User.class));
		
		assertThat(result).isNotNull()
		.extracting(UserDTO::getId, UserDTO::getUsername, UserDTO::getEmail, UserDTO::getRole, UserDTO::getActive)
		.containsExactly(1L, NEW_USERNAME, NEW_EMAIL, Role.CUSTOMER, true);
	}
	
	@Test
	void testSuccessfullyGetUserProfile() {
		UserDTO userDTO = UserDTO.builder()
				.id(1L)
				.username(USERNAME)
				.role(Role.CUSTOMER)
				.active(true).build();
		
		when(userAuthentication.getAuthentication()).thenReturn(auth);
		when(userRepository.findByUsername(anyString())).thenReturn(
				Optional.of(new User(USERNAME, PASSWORD, EMAIL, FIRSTNAME, LASTNAME, ADDRESS, Role.CUSTOMER, true)));
		when(userMapper.toDTO(any(User.class))).thenReturn(userDTO);
		
		UserDTO result = userService.getUserProfile();
		
		assertThat(result).isNotNull()
		.extracting(UserDTO::getId, UserDTO::getUsername, UserDTO::getRole, UserDTO::getActive)
		.containsExactly(1L, USERNAME, Role.CUSTOMER, true);
	}
	
}
