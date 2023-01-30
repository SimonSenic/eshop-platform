package com.eshop.platform.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.eshop.platform.dto.UpdateUserDTO;
import com.eshop.platform.dto.UserDTO;
import com.eshop.platform.entity.User;

@Mapper(componentModel="spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {
	UserDTO toDTO(User user);
	User toUser(UserDTO userDTO);
	User updateUser(@MappingTarget User user, UpdateUserDTO updateUserDTO);
	List<UserDTO> toDTOs(List<User> users);
	
}
