package com.vs.service.mapper;

import com.vs.model.UserInfo;
import com.vs.user.dto.RegisterRequest;
import com.vs.user.dto.RegisterResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class UserMapper {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Mapping(target = "username", source = "username")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "password", expression = "java(encodePassword(userRequest))")
    @Mapping(target = "created", expression = "java(java.time.Instant.now())")
    @Mapping(target = "enabled", expression = "java(false)")
    public abstract UserInfo map(RegisterRequest userRequest);

    public String encodePassword(RegisterRequest userRequest) {
        return passwordEncoder.encode(userRequest.getPassword());
    }

    @Mapping(target = "id", source = "id")
    @Mapping(target = "username", source = "username")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "password", source = "password")
    @Mapping(target = "enabled", source = "enabled")
    public abstract RegisterResponse map(UserInfo user);
}
