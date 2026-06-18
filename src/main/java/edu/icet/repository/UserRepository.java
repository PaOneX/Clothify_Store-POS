package edu.icet.repository;

import edu.icet.model.dto.UserDto;

import java.util.Optional;

public interface UserRepository {
    Optional<UserDto> findByUsername(String username);
    int createUser(UserDto user);
    void deleteByEmployeeId(Integer employeeId);
    void incrementFailedAttempts(String username);
    void resetFailedAttempts(String username);
    int getFailedAttempts(String username);
    void lockAccount(String username, int minutes);
    boolean isLocked(String username);
    void updatePasswordHash(Integer userId, String hash);
}
