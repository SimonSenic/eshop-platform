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
public class AdminController {
	private final UserService userService;
	private final UserMapper userMapper;
	private final PasswordEncoder passwordEncoder;
	
	@GetMapping("/users")
	public ResponseEntity<List<UserDTO>> getUsers(@RequestHeader(value="Authorization") String header){
		List<UserDTO> list = userMapper.toDTOs(userService.findAll()); 
		return ResponseEntity.ok(list);
	}
	
	@GetMapping("/users/{id}")
	public ResponseEntity<UserDTO> getUser(@RequestHeader(value="Authorization") String header, @PathVariable(value = "id") Long id){
		User user = userService.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));
		UserDTO userDTO = userMapper.toDTO(user);
		return ResponseEntity.ok(userDTO);
	}
	
	@PostMapping("/create-admin")
	public ResponseEntity<UserDTO> createAdmin(@RequestHeader(value="Authorization") String header, @RequestBody @Valid UserDTO body) {
		if(userService.findByEmail(body.getEmail()).isPresent()) throw new IllegalStateException("Email is occupied");
		User user = new User(body.getEmail(), Role.ADMIN);
		userService.save(user);
		UserDTO userDTO = userMapper.toDTO(user);
		return ResponseEntity.status(HttpStatus.CREATED).body(userDTO);
	}
}
