package com.eshop.userservice.integration;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.eshop.userservice.entity.Role;
import com.eshop.userservice.entity.User;

@TestConfiguration
class JwtTestComponent {
	
	private final String secret;
	
	public JwtTestComponent(@Value("${secret.key}") String secret) {
		this.secret = secret;
	}
	
	public String generateCustomerToken(User customerUser) {
		return generateJwtToken(customerUser, Role.CUSTOMER);
	}
	
	public String generateAdminToken(User adminUser) {
		return generateJwtToken(adminUser, Role.ADMIN);
	}
	
	public String generateExpiredCustomerToken(User customerUser) {
		return generateExpiredJwtToken(customerUser, Role.CUSTOMER);
	}
	
	public String generateExpiredAdminToken(User adminUser) {
		return generateExpiredJwtToken(adminUser, Role.ADMIN);
	}

	private String generateJwtToken(User user, Role role) {
		Algorithm algorithm = Algorithm.HMAC256(secret.getBytes());
		String username = user.getUsername();
		
	    return JWT.create()
	    		.withSubject(username)
	    		.withExpiresAt(new Date(System.currentTimeMillis() + 1 * 60 * 60 * 1000))
	    		.withClaim("roles", List.of(role.toString()))
	            .sign(algorithm);
	}
	
	private String generateExpiredJwtToken(User user, Role role) {
		Algorithm algorithm = Algorithm.HMAC256(secret.getBytes());
		String username = user.getUsername();
		
	    return JWT.create()
	    		.withSubject(username)
	    		.withExpiresAt(new Date(System.currentTimeMillis() - 1 * 60 * 60 * 1000))
	    		.withClaim("roles", List.of(role.toString()))
	            .sign(algorithm);
	}
	
}
