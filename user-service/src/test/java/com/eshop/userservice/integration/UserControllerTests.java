package com.eshop.userservice.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.bind.MethodArgumentNotValidException;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.eshop.userservice.dto.UpdateUserDTO;
import com.eshop.userservice.dto.UserDTO;
import com.eshop.userservice.dto.UserEmailDTO;
import com.eshop.userservice.entity.Role;
import com.eshop.userservice.entity.User;
import com.eshop.userservice.exception.BusinessException;
import com.eshop.userservice.exception.NotFoundException;
import com.eshop.userservice.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Import({ JwtTestComponent.class })
public class UserControllerTests extends AbstractIntegrationTests {
	
	@Autowired
    private MockMvc mockMvc;
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
    
	@Autowired
    private UserRepository userRepository;
	
	@Autowired
	private JwtTestComponent jwtTestComponent;
    
	@Autowired
    private ObjectMapper objectMapper;
    
	@Autowired
    private PasswordEncoder passwordEncoder;
	
	@Autowired
	private Environment environment;
	
	private final String CUSTOMER_USERNAME = "User123";
	private final String CUSTOMER_PASSWORD = "Password123!";
	private final String CUSTOMER_EMAIL = "user@gmail.com";
	private final String CUSTOMER_FIRSTNAME = "Firstname";
	private final String CUSTOMER_LASTNAME = "Lastname";
	private final String CUSTOMER_ADDRESS = "Address 123";
	private final String ADMIN_USERNAME = "Admin123";
	private final String ADMIN_PASSWORD = "Password!123";
	private final String ADMIN_EMAIL = "admin@gmail.com";
	private final String NEW_CUSTOMER_USERNAME = "NewUser123";
	private final String NEW_CUSTOMER_PASSWORD = "NewPassword123!";
	private final String NEW_CUSTOMER_EMAIL = "new.user@gmail.com";
	
	private User customerUser;
	private User adminUser;
	
	private String customerUserJwt;
	private String adminUserJwt;
	
	@BeforeEach
	void setup() {
	    jdbcTemplate.execute("TRUNCATE TABLE users RESTART IDENTITY CASCADE;");
	    
	    customerUser = userRepository.save(createCustomerUser());
		customerUserJwt = jwtTestComponent.generateCustomerToken(customerUser);
	    
	    adminUser = userRepository.save(createAdminUser());
		adminUserJwt = jwtTestComponent.generateAdminToken(adminUser);
	}
	
	@Test
    void testSuccessfullyLoginAsCustomer() throws Exception {
		UserDTO customerUserDTO = buildUserLogin(CUSTOMER_USERNAME, CUSTOMER_PASSWORD);

        mockMvc.perform(post("/user-service/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(customerUserDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").exists())
                .andExpect(jsonPath("$.refresh_token").exists())
                .andExpect(jsonPath("$.role").value("CUSTOMER"));
    }
	
	@Test
    void testSuccessfullyLoginAsAdmin() throws Exception { 
		UserDTO adminUserDTO = buildUserLogin(ADMIN_USERNAME, ADMIN_PASSWORD);

        mockMvc.perform(post("/user-service/user/login")      	
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adminUserDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").exists())
                .andExpect(jsonPath("$.refresh_token").exists())
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }
	
	@Test
    void testFailLoginWithInvalidUser() throws Exception { 
		UserDTO customerUserDTO = buildUserLogin("InvalidUser", CUSTOMER_PASSWORD);

        mockMvc.perform(post("/user-service/user/login")      	
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(customerUserDTO)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Bad credentials"));
    }
	
	@Test
    void testFailLoginWithInvalidPassword() throws Exception { 
		UserDTO customerUserDTO = buildUserLogin(CUSTOMER_USERNAME, "InvalidPassword123!");

        mockMvc.perform(post("/user-service/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(customerUserDTO)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Bad credentials"));
    }
	
	@Test
    void testSuccessfullyRefreshTokenAsCustomer() throws Exception { 
        mockMvc.perform(get("/user-service/user/auth-refresh")      	
        		.header(HttpHeaders.AUTHORIZATION, "Bearer " +customerUserJwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").exists())
                .andExpect(jsonPath("$.refresh_token").exists())
                .andExpect(jsonPath("$.role").value("CUSTOMER"));
    }
	
	@Test
    void testSuccessfullyRefreshTokenAsAdmin() throws Exception { 
        mockMvc.perform(get("/user-service/user/auth-refresh")      	
        		.header(HttpHeaders.AUTHORIZATION, "Bearer " +adminUserJwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").exists())
                .andExpect(jsonPath("$.refresh_token").exists())
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }
	
	@Test
    void testFailRefreshTokenWithInvalidJwt() throws Exception { 
		customerUserJwt = jwtTestComponent.generateExpiredCustomerToken(customerUser);
		
		mockMvc.perform(get("/user-service/user/auth-refresh")      	
        		.header(HttpHeaders.AUTHORIZATION, "Bearer " +customerUserJwt))
                .andExpect(status().isUnauthorized());
    }
	
	@Test
	void testSuccessfullyUpdateUser() throws Exception {
		UpdateUserDTO updateUserDTO = buildUserUpdate(NEW_CUSTOMER_USERNAME, CUSTOMER_PASSWORD, NEW_CUSTOMER_PASSWORD, NEW_CUSTOMER_EMAIL);
		
		MvcResult mvcResult = mockMvc.perform(patch("/user-service/user/update")
        		.header(HttpHeaders.AUTHORIZATION, "Bearer " +customerUserJwt)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateUserDTO)))
                .andExpect(status().isOk()).andReturn();
		
		UserDTO resultDTO = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), UserDTO.class);

        assertThat(resultDTO).isNotNull()
		.extracting(UserDTO::getId, UserDTO::getUsername, UserDTO::getEmail, UserDTO::getAddress)
		.containsExactly(1L, NEW_CUSTOMER_USERNAME, NEW_CUSTOMER_EMAIL, CUSTOMER_ADDRESS);

        User dbResult = userRepository.findById(resultDTO.getId())
        		.orElseThrow(() -> new AssertionError("User not found"));
        
        assertThat(dbResult)
		.extracting(User::getId, User::getUsername, User::getEmail, User::getAddress)
		.containsExactly(1L, NEW_CUSTOMER_USERNAME, NEW_CUSTOMER_EMAIL, CUSTOMER_ADDRESS);
        assertThat(passwordEncoder.matches(NEW_CUSTOMER_PASSWORD, dbResult.getPassword())).isTrue();
	}
	
	@Test
	void testFailUpdateUserWithAlreadyOccupiedUsername() throws Exception {
		UpdateUserDTO updateUserDTO = buildUserUpdate(ADMIN_USERNAME, null, null, null);
		
		MvcResult mvcResult = mockMvc.perform(patch("/user-service/user/update")
        		.header(HttpHeaders.AUTHORIZATION, "Bearer " +customerUserJwt)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateUserDTO)))
                .andExpect(status().isBadRequest()).andReturn();
		
		assertThat(mvcResult.getResolvedException()).isNotNull()
		.isInstanceOf(BusinessException.class);
	}
	
	@Test
	void testFailUpdateUserWithAlreadyOccupiedEmail() throws Exception {
		UpdateUserDTO updateUserDTO = buildUserUpdate(null, null, null, ADMIN_EMAIL);
		
		MvcResult mvcResult = mockMvc.perform(patch("/user-service/user/update")
        		.header(HttpHeaders.AUTHORIZATION, "Bearer " +customerUserJwt)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateUserDTO)))
                .andExpect(status().isBadRequest()).andReturn();
		
		assertThat(mvcResult.getResolvedException()).isNotNull()
		.isInstanceOf(BusinessException.class);
	}
	
	void testFailUpdateUserWithInvalidPassword() throws Exception {
		UpdateUserDTO updateUserDTO = buildUserUpdate(null, "InvalidPassword123!", NEW_CUSTOMER_PASSWORD, null);
		
		MvcResult mvcResult = mockMvc.perform(patch("/user-service/user/update")
        		.header(HttpHeaders.AUTHORIZATION, "Bearer " +customerUserJwt)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateUserDTO)))
                .andExpect(status().isBadRequest()).andReturn();
		
		assertThat(mvcResult.getResolvedException()).isNotNull()
		.isInstanceOf(BusinessException.class);
	}
	
	@Test
	void testFailUpdateUserWithInvalidNewPassword() throws Exception {
		UpdateUserDTO updateUserDTO = buildUserUpdate(null, CUSTOMER_PASSWORD, "0", null);
		
		MvcResult mvcResult = mockMvc.perform(patch("/user-service/user/update")
        		.header(HttpHeaders.AUTHORIZATION, "Bearer " +customerUserJwt)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateUserDTO)))
                .andExpect(status().isBadRequest()).andReturn();
		
		assertThat(mvcResult.getResolvedException()).isNotNull()
		.isInstanceOf(MethodArgumentNotValidException.class);
	}
	
	@Test
	void testSuccessfullyRecoverPassword() throws Exception {
		UserEmailDTO userEmailDTO = new UserEmailDTO();
		userEmailDTO.setEmail(CUSTOMER_EMAIL);
		
		mockMvc.perform(post("/user-service/user/recover-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userEmailDTO)))
                .andExpect(status().isOk());
	}
	
	@Test
	void testFailRecoverPasswordWithInvalidEmail() throws Exception {
		UserEmailDTO userEmailDTO = new UserEmailDTO();
		userEmailDTO.setEmail("invalid.email@gmail.com");
		
		MvcResult mvcResult = mockMvc.perform(post("/user-service/user/recover-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userEmailDTO)))
                .andExpect(status().isNotFound()).andReturn();
		
		assertThat(mvcResult.getResolvedException()).isNotNull()
		.isInstanceOf(NotFoundException.class);
	}
	
	@Test
	void testSuccessfullySetNewPassword() throws Exception {
		UpdateUserDTO updateUserDTO = UpdateUserDTO.builder()
				.password(NEW_CUSTOMER_PASSWORD).build();

		String verificationToken = generateVerificationToken(customerUser);
		
		mockMvc.perform(patch("/user-service/user/set-new-password")
        		.param("verificationToken", verificationToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateUserDTO)))
                .andExpect(status().isOk());
		
		User dbResult = userRepository.findById(customerUser.getId())
        		.orElseThrow(() -> new AssertionError("User not found"));
        
        assertThat(passwordEncoder.matches(NEW_CUSTOMER_PASSWORD, dbResult.getPassword())).isTrue();
	}
	
	@Test
	void testFailSetNewPasswordWithInvalidPassword() throws Exception {
		UpdateUserDTO updateUserDTO = UpdateUserDTO.builder()
				.password("0").build();

		String verificationToken = generateVerificationToken(customerUser);
		
		MvcResult mvcResult = mockMvc.perform(patch("/user-service/user/set-new-password")
        		.param("verificationToken", verificationToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateUserDTO)))
                .andExpect(status().isBadRequest()).andReturn();
		
		assertThat(mvcResult.getResolvedException()).isNotNull()
		.isInstanceOf(MethodArgumentNotValidException.class);
	}
	
	@Test
	void testFailSetNewPasswordWithInvalidToken() throws Exception {
		UpdateUserDTO updateUserDTO = UpdateUserDTO.builder()
				.password(NEW_CUSTOMER_PASSWORD).build();

		String verificationToken = generateExpiredVerificationToken(customerUser);
		
		MvcResult mvcResult = mockMvc.perform(patch("/user-service/user/set-new-password")
        		.param("verificationToken", verificationToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateUserDTO)))
                .andExpect(status().isBadRequest()).andReturn();
		
		assertThat(mvcResult.getResolvedException()).isNotNull()
		.isInstanceOf(BusinessException.class);
	}
	
	@Test
	void testSuccessfullyGetProfile() throws Exception {
		MvcResult mvcResult = mockMvc.perform(get("/user-service/user/profile")
        		.header(HttpHeaders.AUTHORIZATION, "Bearer " +customerUserJwt))
                .andExpect(status().isOk()).andReturn();
		
		UserDTO resultDTO = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), UserDTO.class);

        assertThat(resultDTO).isNotNull()
		.extracting(UserDTO::getId, UserDTO::getUsername, UserDTO::getEmail, UserDTO::getRole)
		.containsExactly(1L, CUSTOMER_USERNAME, CUSTOMER_EMAIL, Role.CUSTOMER);
	}
	
	@Test
	void testFailGetProfileWithInvalidJwt() throws Exception {
		customerUserJwt = jwtTestComponent.generateExpiredCustomerToken(customerUser);
		
		mockMvc.perform(get("/user-service/user/profile")
        		.header(HttpHeaders.AUTHORIZATION, "Bearer " +customerUserJwt))
                .andExpect(status().isUnauthorized());
	}
	
	private UserDTO buildUserLogin(String username, String password) {
		return UserDTO.builder()
        		.username(username)
        		.password(password).build();
	}
	
	private UpdateUserDTO buildUserUpdate(String username, String password, String newPassword, String email) {
		return UpdateUserDTO.builder()
				.username(username)
				.password(password)
				.newPassword(newPassword)
				.email(email).build();
	}
	
	private User createAdminUser() {
		User user = new User();
		user.setUsername(ADMIN_USERNAME);
		user.setPassword(passwordEncoder.encode(ADMIN_PASSWORD));
		user.setEmail(ADMIN_EMAIL);
		user.setRole(Role.ADMIN);
		user.setActive(true);
		return user;
	}
	
	private User createCustomerUser() {
		User user = new User();
		user.setUsername(CUSTOMER_USERNAME);
		user.setPassword(passwordEncoder.encode(CUSTOMER_PASSWORD));
		user.setEmail(CUSTOMER_EMAIL);
		user.setFirstName(CUSTOMER_FIRSTNAME);
		user.setLastName(CUSTOMER_LASTNAME);
		user.setAddress(CUSTOMER_ADDRESS);
		user.setRole(Role.CUSTOMER);
		user.setActive(true);
		return user;
	}
	
	private String generateVerificationToken(User user) {
		Algorithm algorithm = Algorithm.HMAC256(environment.getProperty("verification.secret.key").getBytes());
		String subject = user.getUsername();
		
	    return JWT.create()
	    		.withSubject(subject)
	    		.withExpiresAt(new Date(System.currentTimeMillis() + 1 * 60 * 60 * 1000))
	            .sign(algorithm);
	}
	
	private String generateExpiredVerificationToken(User user) {
		Algorithm algorithm = Algorithm.HMAC256(environment.getProperty("verification.secret.key").getBytes());
		String subject = user.getUsername();
		
	    return JWT.create()
	    		.withSubject(subject)
	    		.withExpiresAt(new Date(System.currentTimeMillis() - 1 * 60 * 60 * 1000))
	            .sign(algorithm);
	}

}
