package edu.icet.service.Impl;

import edu.icet.model.dto.UserDto;
import edu.icet.repository.UserRepository;
import edu.icet.service.AuditService;
import edu.icet.service.AuthService;
import edu.icet.util.PasswordUtil;

import java.util.Optional;

public class AuthServiceImpl implements AuthService {

    private static final int MAX_ATTEMPTS = 5;

    private final UserRepository userRepository;
    private final AuditService auditService;

    public AuthServiceImpl(UserRepository userRepository, AuditService auditService) {
        this.userRepository = userRepository;
        this.auditService = auditService;
    }

    @Override
    public Optional<UserDto> login(String username, String password) {
        if (userRepository.isLocked(username)) {
            auditService.logLogin(null, username, false);
            throw new IllegalStateException("Account locked. Try again later.");
        }

        Optional<UserDto> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            auditService.logLogin(null, username, false);
            return Optional.empty();
        }

        UserDto user = userOpt.get();
        if (PasswordUtil.verify(password, user.getPasswordHash())) {
            userRepository.resetFailedAttempts(username);
            if (!PasswordUtil.isBcryptHash(user.getPasswordHash())) {
                userRepository.updatePasswordHash(user.getUserId(), PasswordUtil.hash(password));
            }
            user.setPasswordHash(null);
            auditService.logLogin(user.getUserId(), username, true);
            return Optional.of(user);
        }

        userRepository.incrementFailedAttempts(username);
        auditService.logLogin(user.getUserId(), username, false);
        int attempts = userRepository.getFailedAttempts(username);
        if (attempts >= MAX_ATTEMPTS) {
            userRepository.lockAccount(username, 15);
            throw new IllegalStateException("Account locked after too many failed attempts.");
        }
        return Optional.empty();
    }
}
