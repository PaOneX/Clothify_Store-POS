package edu.icet.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeDto {
    private Integer employeeId;
    private String name;
    private String phone;
    private String email;
    private String address;
    private LocalDate hireDate;
    private boolean active;
}
