package com.eshop.userservice.controller;

import java.io.IOException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eshop.userservice.dto.UpdateUserDTO;
import com.eshop.userservice.dto.UserDTO;
import com.eshop.userservice.service.UserService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/user-service/user")
@AllArgsConstructor
public class UserController {
	private final UserService userService;
	
	@PostMapping("/login")
	public ResponseEntity<UserDTO> login(@RequestBody UserDTO userDTO){
		return ResponseEntity.ok(userService.login(userDTO));
	}
	
	@GetMapping("/auth-refresh")
	public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		userService.refreshToken(request, response);
	}
	
	@PatchMapping("/update") //update passwrd neprihlaseneho admina? cez token
	public ResponseEntity<UserDTO> updateUser(@RequestBody @Valid UpdateUserDTO updateUserDTO){ 
		return ResponseEntity.ok(userService.updateUser(updateUserDTO));
	} 
	
	@GetMapping("/profile")
	public ResponseEntity<UserDTO> getUserProfile(){
		return ResponseEntity.ok(userService.getUserProfile());
	}
	
}
