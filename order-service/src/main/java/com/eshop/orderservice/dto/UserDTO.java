package com.eshop.orderservice.dto;

import lombok.Data;

@Data
public class UserDTO {
	private Long id;
	
	private String username;
	
	private String password;
	
	private String email;
	
	private String firstName;
	
	private String lastName;
	
	private String address;
}
