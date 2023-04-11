package com.eshop.userservice.security;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class AuthenticationFilter extends AbstractAuthenticationProcessingFilter {
	private AuthenticationManager authenticationManager;

	public AuthenticationFilter(AuthenticationManager authenticationManager) {
		super("/user-service/user/login");
	    this.authenticationManager = authenticationManager;
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
			throws AuthenticationException {
		String body = null;
	    try { body = request.getReader().lines().collect(Collectors.joining()); }
	    catch (Exception e) { e.printStackTrace(); }

	    JSONObject jsonObject = new JSONObject(body);
	    String username = jsonObject.getString("username");
	    String password = jsonObject.getString("password");
	    UsernamePasswordAuthenticationToken authenticationToken =
	    	new UsernamePasswordAuthenticationToken(username, password);

	    return authenticationManager.authenticate(authenticationToken);
	}

	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
	        FilterChain chain, Authentication authResult) throws IOException, ServletException {
	    User user = (User) authResult.getPrincipal();
	    Algorithm algorithm = Algorithm.HMAC256("${secret.key}".getBytes());
	    String access_token = JWT.create()
	    		.withSubject(user.getUsername())
	    		.withExpiresAt(new Date(System.currentTimeMillis() + 20 * 60 * 1000))
	            .withIssuer(request.getRequestURL().toString())
	            .withClaim("roles", user.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()))
	            .sign(algorithm);
	    String refresh_token = JWT.create()
	    		.withSubject(user.getUsername())
	    		.withExpiresAt(new Date(System.currentTimeMillis() + 6 * 60 * 60 * 1000))
	            .withIssuer(request.getRequestURL().toString())
	            .sign(algorithm);
	   
	    Map<String, String> map = new HashMap<>();
	    map.put("access_token", access_token);
	    map.put("refresh_token", refresh_token);
	    map.put("role", user.getAuthorities().stream().map(GrantedAuthority::getAuthority).findAny().get());
	    response.setContentType("application/json");
	    new ObjectMapper().writeValue(response.getOutputStream(), map);
	}
	
	@Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, 
    		AuthenticationException authenticationException) throws IOException, ServletException {
		Map<String, String> map = new HashMap<>();
		map.put("description", request.getRequestURI());
		map.put("error", authenticationException.getMessage());
		response.setContentType("application/json");
		response.setStatus(401);
	    new ObjectMapper().writeValue(response.getOutputStream(), map);
    }
	 
}
