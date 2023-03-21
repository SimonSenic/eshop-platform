package com.eshop.userservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eshop.userservice.dto.UserDTO;
import com.eshop.userservice.service.CustomerService;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/user-service/customer")
@AllArgsConstructor
public class CustomerController {
	private final CustomerService customerService;
	
	@PostMapping("/register")
	public ResponseEntity<UserDTO> registerCustomer(@RequestBody @Valid UserDTO userDTO) {
		return ResponseEntity.status(HttpStatus.CREATED).body(customerService.registerCustomer(userDTO));
	}
}
