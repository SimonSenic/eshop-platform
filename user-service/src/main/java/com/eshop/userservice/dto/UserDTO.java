package com.eshop.userservice.dto;

import static com.eshop.userservice.dto.UserConstants.*;

import com.eshop.userservice.entity.Role;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class UserDTO {
	private Long id;
	
	@NotNull(message = USERNAME_NOT_NULL)
	@Size(min = 3, max = 30, message = USERNAME_SIZE)
	private String username;
	
	@NotNull(message = PASSWORD_NOT_NULL)
	@Pattern(regexp = PASSWORD_REGEXP, message = PASSWORD_MSG)
	private String password;
	
	@NotNull(message = EMAIL_NOT_NULL)
	@Email(regexp = EMAIL_REGEXP, message = EMAIL_MSG)
	private String email;
	
	@NotNull(message = FIRSTNAME_NOT_NULL)
	@Size(min = 1, max = 30, message = FIRSTNAME_SIZE)
	private String firstName;
	
	@NotNull(message = LASTNAME_NOT_NULL)
	@Size(min = 1, max = 30, message = LASTNAME_SIZE)
	private String lastName;
	
	@NotNull(message = ADDRESS_NOT_NULL)
	@Size(min = 3, max = 50, message = ADDRESS_SIZE)
	private String address;
	
	private Role role;
}
