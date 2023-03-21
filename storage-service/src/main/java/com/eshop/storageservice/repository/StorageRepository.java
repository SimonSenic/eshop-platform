package com.eshop.storageservice.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.eshop.storageservice.entity.Product;

public interface StorageRepository extends JpaRepository<Product, Long>{
	Optional<Product> findByName(String name);
}
