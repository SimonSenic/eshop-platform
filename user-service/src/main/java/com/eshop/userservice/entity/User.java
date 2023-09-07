package com.eshop.userservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@AllArgsConstructor
@RequiredArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class User extends BaseEntity{
	@Column
	private String username;
	
	@Column
	private String password;
	
	@Column
	@NonNull
	private String email;
	
	@Column
	private String firstName;
	
	@Column
	private String lastName;
	
	@Column
	private String address;
	
	@Enumerated(EnumType.STRING)
	@Column
	@NonNull
	private Role role;
	
	@Column
	@NonNull
	private Boolean active;
	
}
