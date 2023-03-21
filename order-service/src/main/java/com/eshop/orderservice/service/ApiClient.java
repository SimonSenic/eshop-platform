package com.eshop.orderservice.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import com.eshop.orderservice.dto.UserDTO;

@FeignClient(name = "user-service-client", url = "https://localhost:8080/user-service/user")
public interface ApiClient {
	@GetMapping("/profile")
    UserDTO getUserProfile();
	
}
