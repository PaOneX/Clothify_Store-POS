package edu.icet.service.Impl;

import edu.icet.model.dto.EmployeeDto;
import edu.icet.model.dto.UserDto;
import edu.icet.model.enums.UserRole;
import edu.icet.repository.EmployeeRepository;
import edu.icet.repository.UserRepository;
import edu.icet.service.EmployeeService;
import edu.icet.util.PasswordUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository, UserRepository userRepository) {
        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
    }

    @Override
    public int addEmployee(EmployeeDto employee, boolean createLogin, String username, String password) {
        int employeeId = employeeRepository.save(employee);
        if (createLogin) {
            UserDto user = new UserDto();
            user.setUsername(username);
            user.setPasswordHash(PasswordUtil.hash(password));
            user.setRole(UserRole.STAFF);
            user.setEmployeeId(employeeId);
            userRepository.createUser(user);
        }
        return employeeId;
    }

    @Override
    public void updateEmployee(EmployeeDto employee) {
        employeeRepository.update(employee);
    }

    @Override
    public void deleteEmployee(Integer id) {
        userRepository.deleteByEmployeeId(id);
        employeeRepository.deleteById(id);
    }

    @Override
    public ObservableList<EmployeeDto> getAllEmployees() {
        return FXCollections.observableArrayList(employeeRepository.findAll());
    }

    @Override
    public ObservableList<EmployeeDto> searchEmployees(String name) {
        if (name == null || name.isBlank()) {
            return getAllEmployees();
        }
        return FXCollections.observableArrayList(employeeRepository.searchByName(name));
    }
}
