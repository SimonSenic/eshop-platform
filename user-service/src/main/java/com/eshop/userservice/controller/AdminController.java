package com.eshop.userservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.eshop.userservice.dto.UserEmailDTO;
import com.eshop.userservice.dto.UpdateAdminDTO;
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
	public ResponseEntity<UserDTO> createAdmin(@RequestBody @Valid UserEmailDTO userEmailDTO) {
		return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createAdmin(userEmailDTO));
	}
	
	@PutMapping("/complete-registration")
	public void completeRegistration(@RequestBody @Valid UpdateAdminDTO updateAdminDTO, @RequestParam String verificationToken){ 
		adminService.completeRegistration(updateAdminDTO, verificationToken);
	} 
	
	@GetMapping("/get-user/{id}")
	public ResponseEntity<UserDTO> getUser(@PathVariable Long id){
		return ResponseEntity.ok(adminService.getUser(id));
	}
}
