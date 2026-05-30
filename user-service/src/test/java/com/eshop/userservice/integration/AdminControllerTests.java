package com.eshop.userservice.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
import com.eshop.userservice.dto.UpdateAdminDTO;
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
class AdminControllerTests extends AbstractIntegrationTests {
	
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
    
    private final String USERNAME = "Admin123";
	private final String PASSWORD = "Password123!";
	private final String EMAIL = "admin@gmail.com";
	
	private String adminUserJwt;
	private String customerUserJwt;
	
	@BeforeEach
	void setup() {
	    jdbcTemplate.execute("TRUNCATE TABLE users RESTART IDENTITY CASCADE;");
	    
	    User adminUser = userRepository.save(createAdminUser());
		adminUserJwt = jwtTestComponent.generateAdminToken(adminUser);
	}
	
	@Test
    void testSuccessfullyCreateAdmin() throws Exception { 
		UserEmailDTO userEmailDTO = new UserEmailDTO();
		userEmailDTO.setEmail(EMAIL);

        MvcResult mvcResult = mockMvc.perform(post("/user-service/admin/create-admin")
        		.header(HttpHeaders.AUTHORIZATION, "Bearer " +adminUserJwt)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userEmailDTO)))
                .andExpect(status().isCreated()).andReturn();
        
        UserDTO resultDTO = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), UserDTO.class);

        assertThat(resultDTO).isNotNull()
		.extracting(UserDTO::getId, UserDTO::getEmail, UserDTO::getRole, UserDTO::getActive)
		.containsExactly(2L, EMAIL, Role.ADMIN, false);

        User dbResult = userRepository.findById(resultDTO.getId())
        		.orElseThrow(() -> new AssertionError("User not found"));
        
        assertThat(dbResult)
		.extracting(User::getId, User::getEmail)
		.containsExactly(2L, EMAIL);
    }
	
	@Test
	void testFailCreateAdminWithInvalidEmail() throws Exception {
		UserEmailDTO userEmailDTO = new UserEmailDTO();
		userEmailDTO.setEmail("0");

		MvcResult mvcResult = mockMvc.perform(post("/user-service/admin/create-admin")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " +adminUserJwt)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userEmailDTO)))
                .andExpect(status().isBadRequest()).andReturn();
		
		assertThat(mvcResult.getResolvedException()).isNotNull()
		.isInstanceOf(MethodArgumentNotValidException.class);
	}
	
	@Test
	void testFailCreateAdminWithAlreadyOccupiedEmail() throws Exception {
		UserEmailDTO userEmailDTO = new UserEmailDTO();
		userEmailDTO.setEmail(EMAIL);

		mockMvc.perform(post("/user-service/admin/create-admin")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " +adminUserJwt)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userEmailDTO)))
                .andExpect(status().isCreated());
		
		UserEmailDTO newUserEmailDTO = new UserEmailDTO();
		newUserEmailDTO.setEmail(userEmailDTO.getEmail());

		MvcResult mvcResult = mockMvc.perform(post("/user-service/admin/create-admin")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " +adminUserJwt)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newUserEmailDTO)))
                .andExpect(status().isBadRequest()).andReturn();
		
		assertThat(mvcResult.getResolvedException()).isNotNull()
		.isInstanceOf(BusinessException.class);
	}
	
	@Test
	void testSuccessfullyCompleteRegistration() throws Exception {
		User user = userRepository.save(createInactiveAdminUser());
        
        UpdateAdminDTO updateAdminDTO = new UpdateAdminDTO();
        updateAdminDTO.setUsername(USERNAME);
        updateAdminDTO.setPassword(PASSWORD);
        
        String verificationToken = generateVerificationToken(user);
		
        mockMvc.perform(put("/user-service/admin/complete-registration")
        		.param("verificationToken", verificationToken)
        		.contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateAdminDTO)))
                .andExpect(status().isOk());
        
        User confirmedUser = userRepository.findById(user.getId())
        		.orElseThrow(() -> new AssertionError("User not found"));
        
        assertThat(confirmedUser)
        .extracting(User::getId, User::getRole, User::getActive)
		.containsExactly(2L, Role.ADMIN, true);
        assertThat(passwordEncoder.matches(PASSWORD, confirmedUser.getPassword())).isTrue();
	}
	
	@Test
	void testFailCompleteRegistrationWithInvalidToken() throws Exception {
		User user = userRepository.save(createInactiveAdminUser());
        
        UpdateAdminDTO updateAdminDTO = new UpdateAdminDTO();
        updateAdminDTO.setUsername(USERNAME);
        updateAdminDTO.setPassword(PASSWORD);
        
        String verificationToken = generateExpiredVerificationToken(user);
		
        MvcResult mvcConfirmResult = mockMvc.perform(put("/user-service/admin/complete-registration")
        		.param("verificationToken", verificationToken)
        		.contentType(MediaType.APPLICATION_JSON)
        		.content(objectMapper.writeValueAsString(updateAdminDTO)))
                .andExpect(status().isBadRequest()).andReturn();
        
        assertThat(mvcConfirmResult.getResolvedException()).isNotNull()
		.isInstanceOf(BusinessException.class);
	}
	
	@Test
	void testFailCompleteRegistrationWithInvalidPassword() throws Exception {
		User user = userRepository.save(createInactiveAdminUser());
        
        UpdateAdminDTO updateAdminDTO = new UpdateAdminDTO();
        updateAdminDTO.setUsername(USERNAME);
        updateAdminDTO.setPassword("0");
        
        String verificationToken = generateVerificationToken(user);
		
        MvcResult mvcConfirmResult = mockMvc.perform(put("/user-service/admin/complete-registration")
        		.param("verificationToken", verificationToken)
        		.contentType(MediaType.APPLICATION_JSON)
        		.content(objectMapper.writeValueAsString(updateAdminDTO)))
                .andExpect(status().isBadRequest()).andReturn();
        
        assertThat(mvcConfirmResult.getResolvedException()).isNotNull()
		.isInstanceOf(MethodArgumentNotValidException.class);
	}
	
	@Test
	void testFailCompleteRegistrationWithAlreadyOccupiedUsername() throws Exception {
		User user = createInactiveAdminUser();
		user.setUsername(USERNAME);
		user.setPassword(PASSWORD);
		user.setActive(true);
		userRepository.save(user);
		
		User newUser = createInactiveAdminUser();
		newUser.setEmail("new.admin@gmail.com");
		userRepository.save(newUser);
        
        UpdateAdminDTO updateAdminDTO = new UpdateAdminDTO();
        updateAdminDTO.setUsername(USERNAME);
        updateAdminDTO.setPassword(PASSWORD);
        
        String verificationToken = generateVerificationToken(newUser);
		
        MvcResult mvcConfirmResult = mockMvc.perform(put("/user-service/admin/complete-registration")
        		.contentType(MediaType.APPLICATION_JSON)
        		.content(objectMapper.writeValueAsString(updateAdminDTO))
                .param("verificationToken", verificationToken))
                .andExpect(status().isBadRequest()).andReturn();
        
        assertThat(mvcConfirmResult.getResolvedException()).isNotNull()
		.isInstanceOf(BusinessException.class);
	}
	
	@Test
	void testSuccessfullyGetUser() throws Exception {
		User customerUser = userRepository.save(createCustomerUser());
		
		MvcResult mvcResult = mockMvc.perform(get("/user-service/admin/get-user/{id}", customerUser.getId())
				.header(HttpHeaders.AUTHORIZATION, "Bearer " +adminUserJwt))
                .andExpect(status().isOk()).andReturn();
		
		UserDTO resultDTO = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), UserDTO.class);
		
		assertThat(resultDTO).isNotNull()
		.extracting(UserDTO::getId, UserDTO::getRole, UserDTO::getActive)
		.containsExactly(2L, Role.CUSTOMER, true);
	}
	
	@Test
	void testFailGetUserWithInvalidId() throws Exception {
		MvcResult mvcResult = mockMvc.perform(get("/user-service/admin/get-user/{id}", 500L)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " +adminUserJwt))
                .andExpect(status().isNotFound()).andReturn();
		
		assertThat(mvcResult.getResolvedException()).isNotNull()
		.isInstanceOf(NotFoundException.class);
	}
	
	@Test
	void testFailGetUserAsCustomer() throws Exception {
		User customerUser = userRepository.save(createCustomerUser());
		customerUserJwt = jwtTestComponent.generateCustomerToken(customerUser);
		
		mockMvc.perform(get("/user-service/admin/get-user/{id}", customerUser.getId())
				.header(HttpHeaders.AUTHORIZATION, "Bearer " +customerUserJwt))
                .andExpect(status().isForbidden()).andReturn();
	}
	
	private User createInactiveAdminUser() {
		User user = new User();
		user.setEmail(EMAIL);
		user.setRole(Role.ADMIN);
		user.setActive(false);
		return user;
	}
	
	private User createAdminUser() {
		User user = new User();
		user.setUsername("TestAdmin");
		user.setPassword(passwordEncoder.encode("TestPassword123!"));
		user.setEmail("test.admin@gmail.com");
		user.setRole(Role.ADMIN);
		user.setActive(true);
		return user;
	}
	
	private User createCustomerUser() {
		User user = new User();
		user.setUsername("User123");
		user.setPassword(passwordEncoder.encode("Password123!"));
		user.setEmail("user@gmail.com");
		user.setFirstName("Firstname");
		user.setLastName("Lastname");
		user.setAddress("Address 123");
		user.setRole(Role.CUSTOMER);
		user.setActive(true);
		return user;
	}
	
	private String generateVerificationToken(User user) {
		Algorithm algorithm = Algorithm.HMAC256(environment.getProperty("verification.secret.key").getBytes());
		String subject = user.getEmail();
		
	    return JWT.create()
	    		.withSubject(subject)
	    		.withExpiresAt(new Date(System.currentTimeMillis() + 1 * 60 * 60 * 1000))
	            .sign(algorithm);
	}
	
	private String generateExpiredVerificationToken(User user) {
		Algorithm algorithm = Algorithm.HMAC256(environment.getProperty("verification.secret.key").getBytes());
		String subject = user.getEmail();
		
	    return JWT.create()
	    		.withSubject(subject)
	    		.withExpiresAt(new Date(System.currentTimeMillis() - 1 * 60 * 60 * 1000))
	            .sign(algorithm);
	}

}
