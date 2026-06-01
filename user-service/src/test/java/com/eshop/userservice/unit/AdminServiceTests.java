package com.eshop.userservice.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
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

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.eshop.userservice.dto.UpdateAdminDTO;
import com.eshop.userservice.dto.UserDTO;
import com.eshop.userservice.dto.UserEmailDTO;
import com.eshop.userservice.entity.Role;
import com.eshop.userservice.entity.User;
import com.eshop.userservice.exception.BusinessException;
import com.eshop.userservice.exception.NotFoundException;
import com.eshop.userservice.mapper.UserMapper;
import com.eshop.userservice.repository.UserRepository;
import com.eshop.userservice.service.AdminService;

@SpringBootTest
class AdminServiceTests {
	
	@InjectMocks
	private AdminService adminService;
	
	@Mock
	private UserRepository userRepository;
	
	@Mock
	private UserMapper userMapper;
	
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
	void testSuccessfullyCreateAdmin() {
		UserEmailDTO userEmailDTO = new UserEmailDTO();
		userEmailDTO.setEmail(EMAIL);
		
		UserDTO userDTO = UserDTO.builder()
				.id(1L)
        		.email(EMAIL)
        		.role(Role.ADMIN)
        		.active(false).build();
		
		when(userRepository.save(any(User.class))).thenReturn(new User(EMAIL, Role.ADMIN, false));
		when(userMapper.toDTO(any(User.class))).thenReturn(userDTO);
		
		UserDTO result = adminService.createAdmin(userEmailDTO);
		
		verify(userRepository).save(any(User.class));
		
		assertThat(result).isNotNull()
		.extracting(UserDTO::getId, UserDTO::getEmail, UserDTO::getRole, UserDTO::getActive)
		.containsExactly(1L, EMAIL, Role.ADMIN, false);
	}
	
	@Test
	void testFailCreateAdminWithAlreadyOccupiedEmail() {
		UserEmailDTO userEmailDTO = new UserEmailDTO();
		userEmailDTO.setEmail(EMAIL);
		
		when(userRepository.existsByEmail(anyString())).thenReturn(true);
		
		assertThrows(BusinessException.class, () -> adminService.createAdmin(userEmailDTO));
	}
	
	@Test
	void testSuccessfullyCompleteRegistration() {
		User user = createAdminUser();
		String verificationToken = generateVerificationToken(user);
		
		UpdateAdminDTO updateAdminDTO = new UpdateAdminDTO();
		updateAdminDTO.setUsername(USERNAME);
		updateAdminDTO.setPassword(PASSWORD);
		
		User updatedUser = user;
		updatedUser.setId(1L);
		updatedUser.setUsername(USERNAME);
		updatedUser.setPassword(PASSWORD);
		
		when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
		when(userMapper.updateAdmin(any(User.class), any(UpdateAdminDTO.class))).thenReturn(updatedUser);
		when(environment.getProperty(anyString())).thenReturn(SECRET);
		
		adminService.completeRegistration(updateAdminDTO, verificationToken);
		
		verify(userRepository).save(any(User.class));
	}
	
	@Test
	void testFailCompleteRegistrationWithInvalidToken() {
		User user = createAdminUser();
		String verificationToken = generateExpiredVerificationToken(user);
		
		UpdateAdminDTO updateAdminDTO = new UpdateAdminDTO();
		updateAdminDTO.setUsername(USERNAME);
		updateAdminDTO.setPassword(PASSWORD);
		
		when(environment.getProperty(anyString())).thenReturn(SECRET);
		
		assertThrows(BusinessException.class, () -> adminService.completeRegistration(updateAdminDTO, verificationToken));
	}
	
	@Test
	void testFailCompleteRegistrationWithAlreadyOccupiedUsername() {
		User user = createAdminUser();
		String verificationToken = generateVerificationToken(user);
		
		UpdateAdminDTO updateAdminDTO = new UpdateAdminDTO();
		updateAdminDTO.setUsername(USERNAME);
		updateAdminDTO.setPassword(PASSWORD);
		
		when(userRepository.existsByUsername(anyString())).thenReturn(true);
		when(environment.getProperty(anyString())).thenReturn(SECRET);
		
		assertThrows(BusinessException.class, () -> adminService.completeRegistration(updateAdminDTO, verificationToken));
	}
	
	@Test
	void testSuccessfullyGetUser() {
		UserDTO userDTO = UserDTO.builder()
				.id(1L)
        		.username(USERNAME)
        		.password(ENCODED_PASSWORD)
        		.email(EMAIL)
        		.firstName(FIRSTNAME)
        		.lastName(LASTNAME)
        		.address(ADDRESS)
        		.role(Role.CUSTOMER)
        		.active(true).build();
		
		when(userRepository.findById(anyLong())).thenReturn(
				Optional.of(new User(USERNAME, PASSWORD, EMAIL, FIRSTNAME, LASTNAME, ADDRESS, Role.CUSTOMER, true)));
		when(userMapper.toDTO(any(User.class))).thenReturn(userDTO);
		
		UserDTO result = adminService.getUser(1L);
		
		assertThat(result).isNotNull()
		.extracting(UserDTO::getId, UserDTO::getUsername, UserDTO::getPassword, UserDTO::getRole, UserDTO::getActive)
		.containsExactly(1L, USERNAME, ENCODED_PASSWORD, Role.CUSTOMER, true);
	}
	
	@Test
	void testFailGetUserWithInvalidId() {
		assertThrows(NotFoundException.class, () -> adminService.getUser(500L));
	}
	
	private User createAdminUser() {
		User user = new User();
		user.setEmail(EMAIL);
		user.setRole(Role.ADMIN);
		user.setActive(false);
		return user;
	}
	
	private String generateVerificationToken(User user) {
		Algorithm algorithm = Algorithm.HMAC256("test-secret".getBytes());
		String subject = user.getEmail();
		
	    return JWT.create()
	    		.withSubject(subject)
	    		.withExpiresAt(new Date(System.currentTimeMillis() + 1 * 60 * 60 * 1000))
	            .sign(algorithm);
	}

	private String generateExpiredVerificationToken(User user) {
		Algorithm algorithm = Algorithm.HMAC256("test-secret".getBytes());
		String subject = user.getEmail();
		
	    return JWT.create()
	    		.withSubject(subject)
	    		.withExpiresAt(new Date(System.currentTimeMillis() - 1 * 60 * 60 * 1000))
	            .sign(algorithm);
	}
	
}
