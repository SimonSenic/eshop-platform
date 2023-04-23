package com.eshop.storageservice.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.eshop.storageservice.dto.ProductDTO;
import com.eshop.storageservice.service.StorageService;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/storage-service/products")
@AllArgsConstructor
public class StorageController {
	private final StorageService storageService;

	@GetMapping
	public ResponseEntity<Page<ProductDTO>> getProducts(Pageable pageable){
		return ResponseEntity.ok(storageService.getProducts(pageable));
	}
	
	@GetMapping("/{id}")
	public ResponseEntity<ProductDTO> getProduct(@PathVariable Long id){
		return ResponseEntity.ok(storageService.getProduct(id));
	}
	
	@PostMapping("/add")
	public ResponseEntity<ProductDTO> addProduct(@RequestBody @Valid ProductDTO productDTO) {
		return ResponseEntity.status(HttpStatus.CREATED).body(storageService.addProduct(productDTO));
	}
	
	@PatchMapping("/{id}/update")
	public ResponseEntity<ProductDTO> updateProduct(@PathVariable Long id, @RequestBody @Valid ProductDTO productDTO){
		return ResponseEntity.ok(storageService.updateProduct(id, productDTO));
	}
	
	@PostMapping("/{id}/order")
	public void orderProduct(@PathVariable Long id, 
			@RequestParam(required = false, defaultValue = "1") Integer amount){
		storageService.orderProduct(id, amount);
	}
	
	@PutMapping("/{id}/update-availability")
	public void updateAvailability(@PathVariable Long id, @RequestParam Integer increase) {
		storageService.updateAvailability(id, increase);
	}
}
