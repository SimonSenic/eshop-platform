package com.eshop.userservice.service;

import org.apache.logging.log4j.Level;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.eshop.userservice.dto.UserDTO;
import com.eshop.userservice.entity.Role;
import com.eshop.userservice.entity.User;
import com.eshop.userservice.exception.BusinessException;
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
	
	public UserDTO registerCustomer(UserDTO userDTO) {
		if (userRepository.findByUsername(userDTO.getUsername()).isPresent()) {
			throw new BusinessException("Username is already occupied");
		}else if(userRepository.findByEmail(userDTO.getEmail()).isPresent()) {
			throw new BusinessException("Email is already occupied");
		}
		
		User user = new User(userDTO.getUsername(), passwordEncoder.encode(userDTO.getPassword()), userDTO.getEmail(), 
				userDTO.getFirstName(), userDTO.getLastName(), userDTO.getAddress(), Role.CUSTOMER);
		userRepository.save(user);
		log.info("Customer registration successful (userId: {})", user.getId());
		//log.info("Send confirm registration email (userId: {})", user.getId());
		return userMapper.toDTO(user);
	}

}
