package com.eshop.storageservice.entity;

import java.math.BigDecimal;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "products")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Product extends BaseEntity{
	
	@Column
	private String name;
	
	@Column
	private String description;
	
	@Column
	private BigDecimal price;
	
	@Column
	private int availability;
	
	@Column
	@Lob
	private byte[] image;
	
}