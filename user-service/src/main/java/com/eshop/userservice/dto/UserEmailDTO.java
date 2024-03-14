package com.eshop.userservice.dto;

import static com.eshop.userservice.dto.UserConstants.*;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
public class UserEmailDTO {
	@NotNull(message = EMAIL_NOT_NULL)
	@Email(regexp = EMAIL_REGEXP, message = EMAIL_MSG)
	private String email;
}
