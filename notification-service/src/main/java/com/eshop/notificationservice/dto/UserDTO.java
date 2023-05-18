package com.eshop.notificationservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDTO {
	private Long id;
	
	private String username;
	
	private String password;
	
	private String email;
	
	private String firstName;
	
	private String lastName;
	
	private String address;
	
	private Role role;
	
	private Boolean active;
}
