package com.eshop.platform.dto;

import static com.eshop.platform.constants.UserConstants.*;

import com.eshop.platform.enums.Role;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserDTO {
	private Long id;
	
	@Size(min = 3, max = 30, message = USERNAME_SIZE)
	private String username;
	
	@Pattern(regexp = PASSWORD_REGEXP, message = PASSWORD_MSG)
	private String password;
	
	@Email(regexp = EMAIL_REGEXP, message = EMAIL_MSG)
	private String email;
	
	@Size(min = 1, max = 30, message = FIRSTNAME_SIZE)
	private String firstName;
	
	@Size(min = 1, max = 30, message = LASTNAME_SIZE)
	private String lastName;
	
	@Size(min = 1, max = 50, message = ADDRESS_SIZE)
	private String address;
	
	private Role role;
}
