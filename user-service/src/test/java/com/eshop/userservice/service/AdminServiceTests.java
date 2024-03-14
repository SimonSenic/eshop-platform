package com.eshop.userservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

import com.eshop.userservice.dto.UserEmailDTO;
import com.eshop.userservice.dto.UpdateAdminDTO;
import com.eshop.userservice.dto.UserDTO;
import com.eshop.userservice.entity.Role;
import com.eshop.userservice.entity.User;
import com.eshop.userservice.exception.BusinessException;
import com.eshop.userservice.mapper.UserMapper;
import com.eshop.userservice.repository.UserRepository;

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
	void testFailCompleteRegistration() {
		String verificationToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJoYXJyeXBvdHRlciIsInJvbGVzIjpbIkNVU1RPTUVSIl0sImlzcyI6Imh0dHA"
				+ "6Ly9ob3N0LmRvY2tlci5pbnRlcm5hbDo4MDgwL3VzZXItc2VydmljZS91c2VyL2xvZ2luIiwiZXhwIjoxNjkyNjUzMTg0fQ.hQthbAFWTqIBrOsxIJI05__PX3BnVZhBES37baVWwfw";
		
		UpdateAdminDTO updateAdminDTO = UpdateAdminDTO.builder()
				.username(USERNAME)
				.password(PASSWORD).build();
		
		when(environment.getProperty(anyString())).thenReturn("secret");
		
		assertThrows(BusinessException.class, () -> adminService.completeRegistration(updateAdminDTO, verificationToken));
	}
	
	@Test
	void testSuccessfullyGetUser() {
		UserDTO userDTO = UserDTO.builder()
				.id(1L)
        		.username(USERNAME)
        		.password(PASSWORD)
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
		.extracting(UserDTO::getId, UserDTO::getUsername, UserDTO::getRole, UserDTO::getActive)
		.containsExactly(1L, USERNAME, Role.CUSTOMER, true);
	}

}
