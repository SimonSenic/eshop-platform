package com.eshop.orderservice.service.integration;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

@TestConfiguration
class JwtTestComponent {
	
	private final String secret;
	
	public JwtTestComponent(@Value("${secret.key}") String secret) {
		this.secret = secret;
	}
	
	public String generateCustomerToken() {
		return generateJwtToken("TestCustomerUser", "CUSTOMER");
	}
	
	public String generateAdminToken() {
		return generateJwtToken("TestAdminUser", "ADMIN");
	}
	
	public String generateExpiredCustomerToken() {
		return generateExpiredJwtToken("TestCustomerUser", "CUSTOMER");
	}
	
	public String generateExpiredAdminToken() {
		return generateExpiredJwtToken("TestAdminUser", "ADMIN");
	}

	private String generateJwtToken(String username, String role) {
		Algorithm algorithm = Algorithm.HMAC256(secret.getBytes());
		
	    return JWT.create()
	    		.withSubject(username)
	    		.withExpiresAt(new Date(System.currentTimeMillis() + 1 * 60 * 60 * 1000))
	    		.withClaim("roles", List.of(role))
	            .sign(algorithm);
	}
	
	private String generateExpiredJwtToken(String username, String role) {
		Algorithm algorithm = Algorithm.HMAC256(secret.getBytes());
		
	    return JWT.create()
	    		.withSubject(username)
	    		.withExpiresAt(new Date(System.currentTimeMillis() - 1 * 60 * 60 * 1000))
	    		.withClaim("roles", List.of(role))
	            .sign(algorithm);
	}
	
}
