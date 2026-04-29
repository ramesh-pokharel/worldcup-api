package com.worldcup.domain.user;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDto toDto(User user);
    AdminUserDto toAdminDto(User user);
}
