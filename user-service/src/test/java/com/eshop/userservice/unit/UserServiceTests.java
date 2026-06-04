package com.eshop.userservice.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;
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

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.eshop.userservice.dto.UpdateUserDTO;
import com.eshop.userservice.dto.UserDTO;
import com.eshop.userservice.entity.Role;
import com.eshop.userservice.entity.User;
import com.eshop.userservice.exception.BusinessException;
import com.eshop.userservice.exception.NotFoundException;
import com.eshop.userservice.mapper.UserMapper;
import com.eshop.userservice.repository.UserRepository;
import com.eshop.userservice.service.UserAuthentication;
import com.eshop.userservice.service.UserService;

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
	
	private final String ENCODED_PASSWORD = "EncodedPassword";
	private final String SECRET = "test-secret";
	
	private final String NEW_USERNAME = "NewUsername";
	private final String NEW_EMAIL = "new.email@gmail.com";
	private final String NEW_PASSWORD = "NewPassword321!";
	
	private final Authentication auth = new UsernamePasswordAuthenticationToken(
			USERNAME, null, List.of(new SimpleGrantedAuthority(Role.CUSTOMER.toString())));
	
	@Test
	void testSuccessfulUpdate() {
		User user = createUser();
		
		UpdateUserDTO updateUserDTO = UpdateUserDTO.builder()
				.username(NEW_USERNAME)
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
	void testFailUpdateWithAlreadyOccupiedUsername() {
		User user = createUser();
		
		UpdateUserDTO updateUserDTO = UpdateUserDTO.builder()
				.username(NEW_USERNAME)
				.email(NEW_EMAIL).build();
		
		when(userAuthentication.getAuthentication()).thenReturn(auth);
		when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));
		when(userRepository.existsByUsername(anyString())).thenReturn(true);
		
		assertThrows(BusinessException.class, () -> userService.updateUser(updateUserDTO));
	}
	
	@Test
	void testFailUpdateWithAlreadyOccupiedEmail() {
		User user = createUser();
		
		UpdateUserDTO updateUserDTO = UpdateUserDTO.builder()
				.username(NEW_USERNAME)
				.email(NEW_EMAIL).build();
		
		when(userAuthentication.getAuthentication()).thenReturn(auth);
		when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));
		when(userRepository.existsByEmail(anyString())).thenReturn(true);
		
		assertThrows(BusinessException.class, () -> userService.updateUser(updateUserDTO));
	}
	
	@Test
	void testFailUpdateWithInvalidPassword() {
		User user = createUser();
		
		UpdateUserDTO updateUserDTO = UpdateUserDTO.builder()
				.password(PASSWORD)
				.newPassword(NEW_PASSWORD).build();
		
		when(userAuthentication.getAuthentication()).thenReturn(auth);
		when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));
		when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);
		
		assertThrows(BusinessException.class, () -> userService.updateUser(updateUserDTO));
	}
	
	@Test
	void testSuccessfullySetNewPassword() {
		User user = createUser();
		String verificationToken = generateVerificationToken(user);
		
		UpdateUserDTO updateUserDTO = UpdateUserDTO.builder()
				.password(PASSWORD).build();
		
		when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));
		when(environment.getProperty(anyString())).thenReturn(SECRET);
		
		userService.setNewPassword(updateUserDTO, verificationToken);
		
		verify(userRepository).save(any(User.class));
	}
	
	@Test
	void testFailSetNewPasswordWithInvalidToken() {
		User user = createUser();
		String verificationToken = generateExpiredVerificationToken(user);
		
		UpdateUserDTO updateUserDTO = UpdateUserDTO.builder()
				.password(PASSWORD).build();
		
		when(environment.getProperty(anyString())).thenReturn(SECRET);
		
		assertThrows(BusinessException.class, () -> userService.setNewPassword(updateUserDTO, verificationToken));
	}
	
	@Test
	void testSuccessfullyGetUserProfile() {
		User user = createUser();
		
		UserDTO userDTO = UserDTO.builder()
				.id(1L)
				.username(USERNAME)
				.role(Role.CUSTOMER)
				.active(true).build();
		
		when(userAuthentication.getAuthentication()).thenReturn(auth);
		when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
		when(userMapper.toDTO(any(User.class))).thenReturn(userDTO);
		
		UserDTO result = userService.getUserProfile();
		
		assertThat(result).isNotNull()
		.extracting(UserDTO::getId, UserDTO::getUsername, UserDTO::getRole, UserDTO::getActive)
		.containsExactly(1L, USERNAME, Role.CUSTOMER, true);
	}
	
	@Test
	void testFailGetUserProfile() {
		when(userAuthentication.getAuthentication()).thenReturn(auth);
		when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
		
		assertThrows(NotFoundException.class, () -> userService.getUserProfile());
	}
	
	private User createUser() {
		User user = new User();
		user.setUsername(USERNAME);
		user.setPassword(ENCODED_PASSWORD);
		user.setEmail(EMAIL);
		user.setFirstName(FIRSTNAME);
		user.setLastName(LASTNAME);
		user.setAddress(ADDRESS);
		user.setRole(Role.CUSTOMER);
		user.setActive(true);
		return user;
	}
	
	private String generateVerificationToken(User user) {
		Algorithm algorithm = Algorithm.HMAC256(SECRET.getBytes());
		String subject = user.getUsername();
		
	    return JWT.create()
	    		.withSubject(subject)
	    		.withExpiresAt(new Date(System.currentTimeMillis() + 1 * 60 * 60 * 1000))
	            .sign(algorithm);
	}
	
	private String generateExpiredVerificationToken(User user) {
		Algorithm algorithm = Algorithm.HMAC256(SECRET.getBytes());
		String subject = user.getUsername();
		
	    return JWT.create()
	    		.withSubject(subject)
	    		.withExpiresAt(new Date(System.currentTimeMillis() - 1 * 60 * 60 * 1000))
	            .sign(algorithm);
	}
	
}
