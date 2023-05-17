package com.eshop.userservice.service;

import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.eshop.userservice.dto.UpdateUserDTO;
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
public class AdminService {
	private final UserRepository userRepository;
	private final UserMapper userMapper;
	
	public UserDTO createAdmin(UserDTO userDTO) {
		if (userRepository.findByUsername(userDTO.getUsername()).isPresent()) {
			throw new BusinessException("Username is already occupied");
		}else if(userRepository.findByEmail(userDTO.getEmail()).isPresent()) {
			throw new BusinessException("Email is already occupied");
		}
		
		User user = new User(userDTO.getUsername(), userDTO.getEmail(), Role.ADMIN);
		userRepository.save(user);
		log.info("Admin created successfully (userId: {})", user.getId());
		log.info("Send complete registration email (userId: {})", user.getId());
		return userMapper.toDTO(user);
	}
	
	public void completeRegistration(UpdateUserDTO updateUserDTO, String verificationToken) {
		if(verificationToken != null && !verificationToken.equals("")) {
			try{
                Algorithm algorithm = Algorithm.HMAC256("${verification.secret.key}".getBytes());
                JWTVerifier verifier = JWT.require(algorithm).build();
                DecodedJWT decodedJWT = verifier.verify(verificationToken);
                
                User user = userRepository.findByUsername(decodedJWT.getSubject())
                		.orElseThrow(() -> new NotFoundException("User not found"));
                userMapper.updateUser(user, updateUserDTO);
                user.setActive(true);
                userRepository.save(user);
                log.info("Admin registration completed (userId: {})", user.getId());
            }catch (Exception e){
                throw new BusinessException(e.getMessage());
            }
		}
	}
	
	public UserDTO getUser(Long id) {
		User user = userRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("User not found"));
		return userMapper.toDTO(user);
	}

}
