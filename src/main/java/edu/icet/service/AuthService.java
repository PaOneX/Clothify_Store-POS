package edu.icet.service;

import edu.icet.model.dto.UserDto;

import java.util.Optional;

public interface AuthService {
    Optional<UserDto> login(String username, String password);
}
