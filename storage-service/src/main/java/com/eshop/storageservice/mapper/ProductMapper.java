package com.eshop.storageservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.data.domain.Page;

import com.eshop.storageservice.dto.ProductDTO;
import com.eshop.storageservice.entity.Product;

@Mapper(componentModel="spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ProductMapper {
	ProductDTO toDTO(Product product);
	
	default Page<ProductDTO> toDTOs(Page<Product> products){
		return products.map(product -> toDTO(product));
	}
	
	@Mapping(target = "availability", expression = "java(product.getAvailability() + increase)")
	Product updateProduct(@MappingTarget Product product, ProductDTO productDTO, Integer increase);
}
