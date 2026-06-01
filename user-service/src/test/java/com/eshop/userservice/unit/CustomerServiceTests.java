package com.eshop.userservice.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.eshop.userservice.dto.UserDTO;
import com.eshop.userservice.entity.Role;
import com.eshop.userservice.entity.User;
import com.eshop.userservice.exception.BusinessException;
import com.eshop.userservice.mapper.UserMapper;
import com.eshop.userservice.repository.UserRepository;
import com.eshop.userservice.service.CustomerService;

@SpringBootTest
class CustomerServiceTests {
	
	@InjectMocks
	private CustomerService customerService;
	
	@Mock
	private UserRepository userRepository;
	
	@Mock
	private UserMapper userMapper;
	
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

	@Test
	void testSuccessfullyRegisterCustomer() {
		UserDTO userDTO = buildUser();
		
		UserDTO mappedUserDTO = userDTO;
		mappedUserDTO.setId(1L);
		mappedUserDTO.setPassword(ENCODED_PASSWORD);
		mappedUserDTO.setRole(Role.CUSTOMER);
		mappedUserDTO.setActive(false);
		
		when(userRepository.save(any(User.class))).thenReturn(createUser());
		when(userMapper.toDTO(any(User.class))).thenReturn(mappedUserDTO);
		when(passwordEncoder.encode(anyString())).thenReturn(ENCODED_PASSWORD);
		
		UserDTO result = customerService.registerCustomer(userDTO);
		
		verify(userRepository).save(any(User.class));
		
		assertThat(result).isNotNull()
		.extracting(UserDTO::getId, UserDTO::getUsername, UserDTO::getPassword, UserDTO::getRole, UserDTO::getActive)
		.containsExactly(1L, USERNAME, ENCODED_PASSWORD, Role.CUSTOMER, false);
	}
	
	@Test
	void testFailRegisterCustomerWithAlreadyOccupiedEmail() {
		UserDTO userDTO = buildUser();
		
		when(userRepository.existsByEmail(anyString())).thenReturn(true);
		
		assertThrows(BusinessException.class, () -> customerService.registerCustomer(userDTO));
	}
	
	@Test
	void testSuccessfullyConfirmRegistration() {
		User user = createUser();
		String verificationToken = generateVerificationToken(user);
		
		when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));
		when(environment.getProperty(anyString())).thenReturn(SECRET);
		
		customerService.confirmRegistration(verificationToken);
		
		verify(userRepository).save(any(User.class));
	}
	
	@Test
	void testFailConfirmRegistrationWithInvalidToken() {
		User user = createUser();
		String verificationToken = generateExpiredVerificationToken(user);
		
		when(environment.getProperty(anyString())).thenReturn(SECRET);
		
		assertThrows(BusinessException.class, () -> customerService.confirmRegistration(verificationToken));
	}
	
	private UserDTO buildUser() {
		return UserDTO.builder()
        		.username(USERNAME)
        		.password(PASSWORD) 
        		.email(EMAIL)
        		.firstName(FIRSTNAME)
        		.lastName(LASTNAME)
        		.address(ADDRESS).build();
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
		user.setActive(false);
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
