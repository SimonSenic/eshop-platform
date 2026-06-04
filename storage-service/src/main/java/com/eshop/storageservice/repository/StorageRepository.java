package com.eshop.storageservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.eshop.storageservice.entity.Product;

public interface StorageRepository extends JpaRepository<Product, Long>{
	
	boolean existsByName(String name);
	
}
