package com.eshop.userservice.mapper;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class PasswordEncoderMapper {
	private final PasswordEncoder passwordEncoder;

	@EncodedMapping
    public String encode(String value) {
        return passwordEncoder.encode(value);
    }
}
