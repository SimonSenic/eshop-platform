package com.eshop.userservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.eshop.userservice.dto.UserDTO;
import com.eshop.userservice.entity.Role;
import com.eshop.userservice.entity.User;
import com.eshop.userservice.exception.BusinessException;
import com.eshop.userservice.mapper.UserMapper;
import com.eshop.userservice.repository.UserRepository;

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

	@Test
	void testSuccessfullyRegisterCustomer() {
		UserDTO userDTO = UserDTO.builder()
        		.username(USERNAME)
        		.password(PASSWORD) 
        		.email(EMAIL)
        		.firstName(FIRSTNAME)
        		.lastName(LASTNAME)
        		.address(ADDRESS).build();
		
		UserDTO mappedUserDTO = userDTO;
		mappedUserDTO.setId(1L);
		mappedUserDTO.setRole(Role.CUSTOMER);
		mappedUserDTO.setActive(false);
		
		when(userRepository.save(any(User.class))).thenReturn(new User(USERNAME, PASSWORD, EMAIL, FIRSTNAME, LASTNAME, ADDRESS, Role.CUSTOMER, false));
		when(userMapper.toDTO(any(User.class))).thenReturn(mappedUserDTO);
		
		UserDTO result = customerService.registerCustomer(userDTO);
		
		verify(userRepository).save(any(User.class));
		
		assertThat(result).isNotNull()
		.extracting(UserDTO::getId, UserDTO::getUsername, UserDTO::getRole, UserDTO::getActive)
		.containsExactly(1L, USERNAME, Role.CUSTOMER, false);
	}
	
	@Test
	void testFailConfirmRegistration() {
		String verificationToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJoYXJyeXBvdHRlciIsInJvbGVzIjpbIkNVU1RPTUVSIl0sImlzcyI6Imh0dHA"
				+ "6Ly9ob3N0LmRvY2tlci5pbnRlcm5hbDo4MDgwL3VzZXItc2VydmljZS91c2VyL2xvZ2luIiwiZXhwIjoxNjkyNjUzMTg0fQ.hQthbAFWTqIBrOsxIJI05__PX3BnVZhBES37baVWwfw";
		
		when(environment.getProperty(anyString())).thenReturn("secret");
		
		assertThrows(BusinessException.class, () -> customerService.confirmRegistration(verificationToken));
	}
	
}
