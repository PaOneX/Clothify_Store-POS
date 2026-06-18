package edu.icet.model.dto;

import edu.icet.model.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private Integer userId;
    private String username;
    private String passwordHash;
    private UserRole role;
    private Integer employeeId;
    private String employeeName;
}
