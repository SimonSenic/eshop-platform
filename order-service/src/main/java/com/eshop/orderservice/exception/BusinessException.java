package com.eshop.orderservice.exception;

public class BusinessException extends RuntimeException{
	public BusinessException(String message) {
		super(message);
	}
}
