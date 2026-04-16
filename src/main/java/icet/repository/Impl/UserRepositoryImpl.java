package edu.icet.repository.Impl;

import edu.icet.model.dto.UserDto;
import edu.icet.model.enums.UserRole;
import edu.icet.repository.UserRepository;
import edu.icet.util.CrudUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

public class UserRepositoryImpl implements UserRepository {

    @Override
    public Optional<UserDto> findByUsername(String username) {
        return CrudUtil.executeQueryForOptional(
                """
                SELECT u.user_id, u.username, u.password_hash, u.role, u.employee_id, e.name AS employee_name
                FROM `user` u
                LEFT JOIN employee e ON u.employee_id = e.employee_id
                WHERE u.username = ?
                """,
                this::mapRow,
                username
        );
    }

    @Override
    public int createUser(UserDto user) {
        return CrudUtil.executeUpdateWithGeneratedKeys(
                "INSERT INTO `user` (username, password_hash, role, employee_id) VALUES (?,?,?,?)",
                user.getUsername(),
                user.getPasswordHash(),
                user.getRole().name(),
                user.getEmployeeId()
        );
    }

    @Override
    public void deleteByEmployeeId(Integer employeeId) {
        CrudUtil.executeUpdate("DELETE FROM `user` WHERE employee_id = ?", employeeId);
    }

    @Override
    public void incrementFailedAttempts(String username) {
        CrudUtil.executeUpdate("UPDATE `user` SET failed_attempts = failed_attempts + 1 WHERE username = ?", username);
    }

    @Override
    public void resetFailedAttempts(String username) {
        CrudUtil.executeUpdate("UPDATE `user` SET failed_attempts = 0, locked_until = NULL WHERE username = ?", username);
    }

    @Override
    public int getFailedAttempts(String username) {
        return CrudUtil.executeQueryForOptional(
                "SELECT failed_attempts FROM `user` WHERE username = ?",
                rs -> rs.getInt("failed_attempts"),
                username
        ).orElse(0);
    }

    @Override
    public void lockAccount(String username, int minutes) {
        LocalDateTime until = LocalDateTime.now().plusMinutes(minutes);
        CrudUtil.executeUpdate("UPDATE `user` SET locked_until = ? WHERE username = ?",
                Timestamp.valueOf(until), username);
    }

    @Override
    public boolean isLocked(String username) {
        return CrudUtil.executeQueryForOptional(
                "SELECT locked_until FROM `user` WHERE username = ?",
                rs -> {
                    Timestamp ts = rs.getTimestamp("locked_until");
                    return ts != null && ts.toLocalDateTime().isAfter(LocalDateTime.now());
                },
                username
        ).orElse(false);
    }

    @Override
    public void updatePasswordHash(Integer userId, String hash) {
        CrudUtil.executeUpdate("UPDATE `user` SET password_hash = ? WHERE user_id = ?", hash, userId);
    }

    private UserDto mapRow(ResultSet rs) throws SQLException {
        return new UserDto(
                rs.getInt("user_id"),
                rs.getString("username"),
                rs.getString("password_hash"),
                UserRole.valueOf(rs.getString("role")),
                rs.getObject("employee_id") != null ? rs.getInt("employee_id") : null,
                rs.getString("employee_name")
        );
    }
}
