package edu.icet.service;

import edu.icet.model.dto.EmployeeDto;
import edu.icet.model.dto.UserDto;
import javafx.collections.ObservableList;

public interface EmployeeService {
    int addEmployee(EmployeeDto employee, boolean createLogin, String username, String password);
    void updateEmployee(EmployeeDto employee);
    void deleteEmployee(Integer id);
    ObservableList<EmployeeDto> getAllEmployees();
    ObservableList<EmployeeDto> searchEmployees(String name);
}
