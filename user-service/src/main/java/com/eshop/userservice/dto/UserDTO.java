package com.eshop.userservice.dto;

import static com.eshop.userservice.dto.UserConstants.ADDRESS_SIZE;
import static com.eshop.userservice.dto.UserConstants.EMAIL_MSG;
import static com.eshop.userservice.dto.UserConstants.EMAIL_REGEXP;
import static com.eshop.userservice.dto.UserConstants.FIRSTNAME_SIZE;
import static com.eshop.userservice.dto.UserConstants.LASTNAME_SIZE;
import static com.eshop.userservice.dto.UserConstants.PASSWORD_MSG;
import static com.eshop.userservice.dto.UserConstants.PASSWORD_REGEXP;
import static com.eshop.userservice.dto.UserConstants.USERNAME_SIZE;

import com.eshop.userservice.entity.Role;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;


@Data
@Builder
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
