package com.eshop.userservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.eshop.userservice.dto.UpdateUserDTO;
import com.eshop.userservice.dto.UserDTO;
import com.eshop.userservice.entity.User;

@Mapper(componentModel="spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, 
		uses = PasswordEncoderMapper.class)
public interface UserMapper {
	UserDTO toDTO(User user);
	
	@Mapping(target = "password", source = "newPassword", qualifiedBy = EncodedMapping.class)
	User updateUser(@MappingTarget User user, UpdateUserDTO updateUserDTO);
}
