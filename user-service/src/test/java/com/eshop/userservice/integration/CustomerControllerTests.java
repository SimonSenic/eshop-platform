package com.eshop.userservice.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.bind.MethodArgumentNotValidException;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.eshop.userservice.dto.UserDTO;
import com.eshop.userservice.entity.Role;
import com.eshop.userservice.entity.User;
import com.eshop.userservice.exception.BusinessException;
import com.eshop.userservice.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class CustomerControllerTests extends AbstractIntegrationTests {
	
	@Autowired
    private MockMvc mockMvc;
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
    
	@Autowired
    private UserRepository userRepository;
    
	@Autowired
    private ObjectMapper objectMapper;
    
	@Autowired
    private PasswordEncoder passwordEncoder;
	
	@Autowired
	private Environment environment;
    
    private final String USERNAME = "User123";
	private final String PASSWORD = "Password123!";
	private final String EMAIL = "user@gmail.com";
	private final String FIRSTNAME = "Firstname";
	private final String LASTNAME = "Lastname";
	private final String ADDRESS = "Address 123";
	
	@BeforeEach
	void setup() {
	    jdbcTemplate.execute("TRUNCATE TABLE users RESTART IDENTITY CASCADE;");
	}
	
	@Test
    void testSuccessfullyRegisterCustomer() throws Exception {
		UserDTO userDTO = buildUser();

        MvcResult mvcResult = mockMvc.perform(post("/user-service/customer/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isCreated()).andReturn();
        
        UserDTO resultDTO = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), UserDTO.class);

        assertThat(resultDTO).isNotNull()
		.extracting(UserDTO::getId, UserDTO::getUsername, UserDTO::getRole, UserDTO::getActive)
		.containsExactly(1L, USERNAME, Role.CUSTOMER, false);

        User dbResult = userRepository.findById(resultDTO.getId())
        		.orElseThrow(() -> new AssertionError("User not found"));
        
        assertThat(dbResult)
		.extracting(User::getId, User::getEmail, User::getActive)
		.containsExactly(1L, EMAIL, false);
        assertThat(passwordEncoder.matches(PASSWORD, dbResult.getPassword())).isTrue();
    }
	
	@Test
	void testFailRegisterCustomerWithInvalidEmail() throws Exception {
		UserDTO userDTO = buildUser();
		userDTO.setEmail("0");

		MvcResult mvcResult = mockMvc.perform(post("/user-service/customer/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isBadRequest()).andReturn();
		
		assertThat(mvcResult.getResolvedException()).isNotNull()
		.isInstanceOf(MethodArgumentNotValidException.class);
	}
	
	@Test
	void testFailRegisterCustomerWithAlreadyOccupiedEmail() throws Exception {
		User user = userRepository.save(createUser());
		
		UserDTO newUserDTO = buildUser();
		newUserDTO.setUsername("User321");
		newUserDTO.setPassword("Password321!");
		newUserDTO.setEmail(user.getEmail());

		MvcResult mvcResult = mockMvc.perform(post("/user-service/customer/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newUserDTO)))
                .andExpect(status().isBadRequest()).andReturn();
		
		assertThat(mvcResult.getResolvedException()).isNotNull()
		.isInstanceOf(BusinessException.class);
	}
	
	@Test
	void testFailRegisterCustomerWithInvalidPassword() throws Exception {
		UserDTO userDTO = buildUser();
		userDTO.setPassword("0");

		MvcResult mvcResult = mockMvc.perform(post("/user-service/customer/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isBadRequest()).andReturn();
		
		assertThat(mvcResult.getResolvedException()).isNotNull()
		.isInstanceOf(MethodArgumentNotValidException.class);
	}
	
	@Test
	void testSuccessfullyConfirmRegistration() throws Exception {
        User user = userRepository.save(createUser());
        
        String verificationToken = generateVerificationToken(user);
		
        mockMvc.perform(put("/user-service/customer/confirm-registration")
                .param("verificationToken", verificationToken))
                .andExpect(status().isOk());
        
        User confirmedUser = userRepository.findById(user.getId())
        		.orElseThrow(() -> new AssertionError("User not found"));
        
        assertThat(confirmedUser)
		.extracting(User::getId, User::getActive)
		.containsExactly(1L, true);
	}
	
	@Test
	void testFailConfirmRegistrationWithInvalidToken() throws Exception {
        User user = userRepository.save(createUser());
        
        String verificationToken = generateExpiredVerificationToken(user);
		
        MvcResult mvcConfirmResult = mockMvc.perform(put("/user-service/customer/confirm-registration")
                .param("verificationToken", verificationToken))
                .andExpect(status().isBadRequest()).andReturn();
        
        assertThat(mvcConfirmResult.getResolvedException()).isNotNull()
		.isInstanceOf(BusinessException.class);
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
		user.setPassword(passwordEncoder.encode(PASSWORD));
		user.setEmail(EMAIL);
		user.setFirstName(FIRSTNAME);
		user.setLastName(LASTNAME);
		user.setAddress(ADDRESS);
		user.setRole(Role.CUSTOMER);
		user.setActive(false);
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
