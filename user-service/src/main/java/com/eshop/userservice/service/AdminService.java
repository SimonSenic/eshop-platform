package com.eshop.userservice.service;

import org.springframework.stereotype.Service;

import com.eshop.userservice.dto.UserDTO;
import com.eshop.userservice.entity.Role;
import com.eshop.userservice.entity.User;
import com.eshop.userservice.exception.BusinessException;
import com.eshop.userservice.mapper.UserMapper;
import com.eshop.userservice.repository.UserRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
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
		return userMapper.toDTO(user);
	}

}
