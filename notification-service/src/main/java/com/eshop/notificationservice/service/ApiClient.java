package com.eshop.notificationservice.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.eshop.notificationservice.dto.OrderDTO;
import com.eshop.notificationservice.dto.UserDTO;

@FeignClient(name = "api-client", url = "http://localhost:9090")
public interface ApiClient {
	@GetMapping("/user-service/admin/get-user/{id}")
	ResponseEntity<UserDTO> getUser(@PathVariable Long id); //nemam auth sessionu established???
	//mozno specific Email send v log.info a poslat token do topicu???
	//alebo novy endpoint s hasIpAddress appky pre ziskanie dat? userID a orderID mam
	
	@GetMapping("/order-service/orders/{id}")
	ResponseEntity<OrderDTO> getOrder(@PathVariable Long id);
	
}
