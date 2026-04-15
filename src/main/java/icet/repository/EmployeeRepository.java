package edu.icet.repository;

import edu.icet.model.dto.EmployeeDto;
import edu.icet.model.dto.UserDto;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends CrudRepository<EmployeeDto, Integer> {
    List<EmployeeDto> searchByName(String name);
}
