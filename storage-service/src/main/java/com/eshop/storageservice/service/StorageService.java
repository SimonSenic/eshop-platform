package com.eshop.storageservice.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.eshop.storageservice.dto.ProductDTO;
import com.eshop.storageservice.entity.Product;
import com.eshop.storageservice.exception.BusinessException;
import com.eshop.storageservice.exception.NotFoundException;
import com.eshop.storageservice.kafka.Producer;
import com.eshop.storageservice.mapper.ProductMapper;
import com.eshop.storageservice.repository.StorageRepository;
import com.fasterxml.jackson.core.JsonProcessingException;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class StorageService {
	private final StorageRepository storageRepository;
	private final ProductMapper productMapper;
	private final UserAuthentication userAuthentication;
	private final Producer producer;
	
	public Page<ProductDTO> getProducts(Pageable pageable){
		return productMapper.toDTOs(storageRepository.findAll(pageable));
	}
	
	public ProductDTO getProduct(Long id) {
		Product product = storageRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("Product not found"));
		return productMapper.toDTO(product);
	}
	
	public ProductDTO addProduct(ProductDTO productDTO){
		if (storageRepository.findByName(productDTO.getName()).isPresent()) {
			throw new BusinessException("Product already exists");
		}
		
		Product product = new Product(productDTO.getName(), productDTO.getPrice(), productDTO.getAvailability());
		storageRepository.save(product);
		return productMapper.toDTO(product);
	}
	
	public ProductDTO editProduct(Long id, ProductDTO productDTO) {
		Product product = storageRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("Product not found"));
		
		product = productMapper.updateProduct(product, productDTO);
		storageRepository.save(product);
		return productMapper.toDTO(product);
	}
	
	public ProductDTO orderProduct(Long id, Integer amount) {
		Product product = storageRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("Product not found"));
		if(amount == null) { 
			amount = 1;
		}else if(amount > product.getAvailability() || amount < 1) {
			throw new BusinessException("Invalid amount");
		}
		
		storageRepository.save(product);
		ProductDTO productDTO = productMapper.toDTO(product);
		try { producer.sendMessage(productDTO, userAuthentication.getAuthentication().getName(), amount); }
		catch(JsonProcessingException e) { e.printStackTrace(); }
		return productDTO;
	}
	
}
