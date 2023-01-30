package com.eshop.platform.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eshop.platform.dto.UserDTO;
import com.eshop.platform.entity.Role;
import com.eshop.platform.entity.User;
import com.eshop.platform.mapper.UserMapper;
import com.eshop.platform.security.PasswordEncoder;
import com.eshop.platform.service.UserService;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/user-service")
@AllArgsConstructor
public class CustomerController {
	private final UserService userService;
	private final UserMapper userMapper;
	private final PasswordEncoder passwordEncoder;
	
	@PostMapping("/register")
	public ResponseEntity<UserDTO> registerCustomer(@RequestBody @Valid UserDTO body) {
		if (userService.findByUsername(body.getUsername()).isPresent()) throw new IllegalStateException("Name is already occupied");
		else if(userService.findByEmail(body.getEmail()).isPresent()) throw new IllegalStateException("Email is already occupied");
		User user = new User(body.getUsername(), passwordEncoder.bCryptPasswordEncoder().encode(body.getPassword()),
				body.getEmail(), body.getFirstName(), body.getLastName(), body.getAddress(), Role.CUSTOMER);
		userService.save(user);
		//user.setPassword(passwordEncoder.bCryptPasswordEncoder().encode(body.getPassword()));
		UserDTO userDTO = userMapper.toDTO(user);
		return ResponseEntity.status(HttpStatus.CREATED).body(userDTO);
	}
	
	
	//TODO: deactivate user
}
