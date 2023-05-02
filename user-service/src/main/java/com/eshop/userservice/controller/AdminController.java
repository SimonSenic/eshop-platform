package com.eshop.userservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eshop.userservice.dto.UpdateUserDTO;
import com.eshop.userservice.dto.UserDTO;
import com.eshop.userservice.service.AdminService;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/user-service/admin")
@AllArgsConstructor
public class AdminController {
	private final AdminService adminService;
	
	@PostMapping("/create-admin")
	public ResponseEntity<UserDTO> createAdmin(@RequestBody @Valid UserDTO userDTO) {
		return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createAdmin(userDTO));
	}
	
	@PutMapping("/complete-registration") //update passwrd neprihlaseneho admina? cez token
	public ResponseEntity<UserDTO> updateUser(@RequestBody @Valid UpdateUserDTO updateUserDTO){ 
		return ResponseEntity.ok(adminService.createAdmin(null));
	} 
}