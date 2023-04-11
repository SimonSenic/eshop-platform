package com.eshop.orderservice.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import com.eshop.orderservice.dto.ProductDTO;
import com.eshop.orderservice.dto.UserDTO;

@FeignClient(name = "api-client", url = "http://localhost:9090")
public interface ApiClient {
	@GetMapping("/user-service/user/profile")
	ResponseEntity<UserDTO> getUserProfile();
	
	@GetMapping("/user-service/user/profile")
	ResponseEntity<UserDTO> getUserProfile(@RequestHeader(value = "Authorization") String token);
	
	@GetMapping("/storage-service/products/{id}")
	ResponseEntity<ProductDTO> getProduct(@PathVariable(value = "id") Long id);
	
	@PatchMapping("/storage-service/products/{id}/update")
	ProductDTO updateProduct(@PathVariable(value = "id") Long id, 
			@RequestBody ProductDTO productDTO, @RequestParam Integer increase);
	
}
