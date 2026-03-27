package com.eshop.storageservice.service;

import java.io.IOException;
import java.math.RoundingMode;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.eshop.storageservice.dto.ProductDTO;
import com.eshop.storageservice.entity.Product;
import com.eshop.storageservice.exception.BusinessException;
import com.eshop.storageservice.exception.NotFoundException;
import com.eshop.storageservice.kafka.Producer;
import com.eshop.storageservice.mapper.ProductMapper;
import com.eshop.storageservice.repository.StorageRepository;
import com.fasterxml.jackson.core.JsonProcessingException;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
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
	
	public ProductDTO addProduct(ProductDTO productDTO, MultipartFile image) throws IOException{
		if (storageRepository.findByName(productDTO.getName()).isPresent()) {
			throw new BusinessException("Product already exists");
		}else if(image != null && image.getSize() > 1 * 1024 * 1024) { //1 MB
			throw new BusinessException("Maximum image size is 1 MB");
		}
		
		Product product = new Product(productDTO.getName(), productDTO.getDescription(), productDTO.getPrice().setScale(2, RoundingMode.HALF_UP), 
				productDTO.getAvailability(), image != null ? image.getBytes() : null);
		log.info(String.valueOf(product.getImage() +" " +image));
		storageRepository.save(product);
		log.info("Product added successfully (productId: {})", product.getId());
		return productMapper.toDTO(product);
	}
	
	public ProductDTO updateProduct(Long id, ProductDTO productDTO, MultipartFile image) throws IOException {
		Product product = storageRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("Product not found"));
		if(image != null && image.getSize() > 1 * 1024 * 1024) { //1 MB
			throw new BusinessException("Maximum image size is 1 MB");
		}
		
		product = productMapper.updateProduct(product, productDTO);
		product.setImage(image != null ? image.getBytes() : null);
		storageRepository.save(product);
		log.info("Product updated successfully (productId: {})", product.getId());
		return productMapper.toDTO(product);
	}
	
	public void orderProduct(Long id, Integer amount) {
		Product product = storageRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("Product not found"));
		if(amount == null || amount < 1) {
			throw new BusinessException("Invalid amount");
		}
		
		ProductDTO productDTO = productMapper.toDTO(product);
		try { 
			producer.sendMessage(productDTO, amount, userAuthentication.getRequest().getHeader("Authorization")); 
		}catch(JsonProcessingException e) { 
			e.printStackTrace(); 
		}
	}
	
	public void updateAvailability(Long id, Integer increase) {
		Product product = storageRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("Product not found"));
		Integer result = product.getAvailability() + increase;
		
		product.setAvailability(result >= 0 ? result : 0);
		storageRepository.save(product);
	}
	
}
