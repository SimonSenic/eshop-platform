package com.eshop.userservice.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.eshop.userservice.entity.User;

public interface UserRepository extends JpaRepository<User, Long>{
	Optional<User> findById(Long id);
	Optional<User> findByUsername(String username);
	Optional<User> findByEmail(String email);
}
