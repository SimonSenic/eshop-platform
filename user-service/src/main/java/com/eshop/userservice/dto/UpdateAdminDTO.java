package com.eshop.userservice.dto;

import static com.eshop.userservice.dto.UserConstants.PASSWORD_MSG;
import static com.eshop.userservice.dto.UserConstants.PASSWORD_NOT_NULL;
import static com.eshop.userservice.dto.UserConstants.PASSWORD_REGEXP;
import static com.eshop.userservice.dto.UserConstants.USERNAME_SIZE;
import static com.eshop.userservice.dto.UserConstants.USERNAME_NOT_NULL;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateAdminDTO {
	@NotNull(message = USERNAME_NOT_NULL)
	@Size(min = 3, max = 30, message = USERNAME_SIZE)
	private String username;
	
    @NotNull(message = PASSWORD_NOT_NULL)
    @Pattern(regexp = PASSWORD_REGEXP, message = PASSWORD_MSG)
	private String password;
}
