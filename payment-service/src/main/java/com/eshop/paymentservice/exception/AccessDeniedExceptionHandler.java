package com.eshop.paymentservice.exception;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class AccessDeniedExceptionHandler implements AccessDeniedHandler{

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response,
			AccessDeniedException accessDeniedException) throws IOException, ServletException {
		Map<String, String> map = new HashMap<>();
		map.put("description", request.getRequestURI());
		map.put("error", accessDeniedException.getMessage());
		response.setContentType("application/json");
		response.setStatus(403);
	    new ObjectMapper().writeValue(response.getOutputStream(), map);
	}

}
