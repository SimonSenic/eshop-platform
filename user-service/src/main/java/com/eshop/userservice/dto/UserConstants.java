package com.eshop.userservice.dto;

public class UserConstants {
	public static final String USERNAME_NOT_NULL = "Username must not be null";
	public static final String USERNAME_SIZE = "Username's size must be between 3 - 30 characters";
	public static final String PASSWORD_NOT_NULL = "Password must not be null";
	public static final String PASSWORD_REGEXP = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#&()–[{}]:;',?/*~$^+=<>]).{7,50}$";
	public static final String PASSWORD_MSG = "Password's size must be between 7 - 50 characters "
			+ "and contain at least 1 numeric value, 1 uppercase character and 1 special symbol";
	public static final String NEW_PASSWORD_MSG = "New password's size must be between 7 - 50 characters "
			+ "and contain at least 1 numeric value, 1 uppercase character and 1 special symbol";
	public static final String EMAIL_NOT_NULL = "Email must not be null";
	public static final String EMAIL_REGEXP = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
	public static final String EMAIL_MSG = "Email must be in a valid format";
	public static final String FIRSTNAME_NOT_NULL = "Firstname must not be null";
	public static final String FIRSTNAME_SIZE = "First name's size must be between 1 - 30 characters";
	public static final String LASTNAME_NOT_NULL = "Lastname must not be null";
	public static final String LASTNAME_SIZE = "Last name's size must be between 1 - 30 characters";
	public static final String ADDRESS_NOT_NULL = "Address must not be null";
	public static final String ADDRESS_SIZE = "Address's size must be between 3 - 50 characters";
	
}
