package com.eshop.userservice.service;

import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.eshop.userservice.dto.UserDTO;
import com.eshop.userservice.entity.Role;
import com.eshop.userservice.entity.User;
import com.eshop.userservice.exception.BusinessException;
import com.eshop.userservice.exception.NotFoundException;
import com.eshop.userservice.mapper.UserMapper;
import com.eshop.userservice.repository.UserRepository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
public class CustomerService {
	private final UserRepository userRepository;
	private final UserMapper userMapper;
	private final PasswordEncoder passwordEncoder;
	private final Environment environment;
	
	public UserDTO registerCustomer(UserDTO userDTO) {
		if (userRepository.findByUsername(userDTO.getUsername()).isPresent()) {
			throw new BusinessException("Username is already occupied");
		}else if(userRepository.findByEmail(userDTO.getEmail()).isPresent()) {
			throw new BusinessException("Email is already occupied");
		}
		
		User user = new User(userDTO.getUsername(), passwordEncoder.encode(userDTO.getPassword()), userDTO.getEmail(), 
				userDTO.getFirstName(), userDTO.getLastName(), userDTO.getAddress(), Role.CUSTOMER, false);
		userRepository.save(user);
		log.info("Customer registration successful (userId: {})", user.getId());
		log.info("Send confirm registration email (userId: {})", user.getId());
		return userMapper.toDTO(user);
	}
	
	public void confirmRegistration(String verificationToken) {
		if(verificationToken != null && !verificationToken.equals("")) {
			try{
                Algorithm algorithm = Algorithm.HMAC256(environment.getProperty("verification.secret.key").getBytes());            
                JWTVerifier verifier = JWT.require(algorithm).build();
                DecodedJWT decodedJWT = verifier.verify(verificationToken);
                
                User user = userRepository.findByUsername(decodedJWT.getSubject())
                		.orElseThrow(() -> new NotFoundException("User not found"));
                user.setActive(true);
                userRepository.save(user);
                log.info("Customer registration confirmed (userId: {})", user.getId());
            }catch (Exception e){
                throw new BusinessException(e.getMessage());
            }
		}
	}

}
