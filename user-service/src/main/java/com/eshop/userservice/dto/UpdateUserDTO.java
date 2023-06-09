package com.eshop.userservice.dto;

import static com.eshop.userservice.dto.UserConstants.*;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateUserDTO {
    @Size(min = 3, max = 30, message = USERNAME_SIZE)
	private String username;
	
    @NotNull(message = PASSWORD_NOT_NULL)
    @Pattern(regexp = PASSWORD_REGEXP, message = PASSWORD_MSG)
	private String password;
    
    @Pattern(regexp = PASSWORD_REGEXP, message = NEW_PASSWORD_MSG)
	private String newPassword;
    
    @Email(regexp = EMAIL_REGEXP, message = EMAIL_MSG)
	private String email;
    
    @Size(min = 1, max = 30, message = FIRSTNAME_SIZE)
	private String firstName;
    
    @Size(min = 1, max = 30, message = LASTNAME_SIZE)
	private String lastName;
    
    @Size(min = 1, max = 50, message = ADDRESS_SIZE)
	private String address;
}
