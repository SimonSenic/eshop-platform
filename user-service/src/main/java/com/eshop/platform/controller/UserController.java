package com.eshop.platform.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eshop.platform.dto.UpdateUserDTO;
import com.eshop.platform.dto.UserDTO;
import com.eshop.platform.entity.User;
import com.eshop.platform.mapper.UserMapper;
import com.eshop.platform.service.UserService;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/user-service")
@AllArgsConstructor
public class UserController {
	private final UserService userService;
	private final UserMapper userMapper;
	
	@PostMapping("/login")
	public ResponseEntity<UserDTO> login(@RequestBody UserDTO body){
		User user = userService.findByUsername(body.getUsername()).orElseThrow(() -> new IllegalArgumentException("User not found"));
		UserDTO userDTO = userMapper.toDTO(user);
		return ResponseEntity.ok(userDTO);
	}
	
	//update + change password skrz mapper?
	@PatchMapping("/update")
	public ResponseEntity<UserDTO> updateUser(@RequestBody @Valid UpdateUserDTO body){
		User user = userService.findByUsername(body.getUsername()).orElseThrow(() -> new IllegalArgumentException("User not found"));
		if(body.getPassword() == null) ;
		
		user = userMapper.updateUser(user, body);
		userService.save(user);
		UserDTO userDTO = userMapper.toDTO(user);
		return ResponseEntity.ok(userDTO);
	}
}
