package com.eshop.notificationservice.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
public class BaseExceptionHandler{
	
	@ExceptionHandler(NotFoundException.class)
	public ResponseEntity<Object> handleNotFoundException(Exception e, WebRequest r){
		Map<String, String> map = new HashMap<>();
		map.put("description", r.getDescription(false));
		map.put("error", e.getMessage()); 
		return new ResponseEntity<>(map, new HttpHeaders(), HttpStatus.NOT_FOUND);
	}
	
	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<Object> handleBusinessException(Exception e, WebRequest r){
		Map<String, String> map = new HashMap<>();
		map.put("description", r.getDescription(false));
		map.put("error", e.getMessage());
		return new ResponseEntity<>(map, new HttpHeaders(), HttpStatus.BAD_REQUEST);
	}
}
