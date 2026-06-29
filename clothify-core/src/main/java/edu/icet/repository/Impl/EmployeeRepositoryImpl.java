package edu.icet.repository.Impl;

import edu.icet.model.dto.EmployeeDto;
import edu.icet.repository.EmployeeRepository;
import edu.icet.util.CrudUtil;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class EmployeeRepositoryImpl implements EmployeeRepository {

    @Override
    public List<EmployeeDto> findAll() {
        return CrudUtil.executeQueryForList("SELECT * FROM employee ORDER BY employee_id", this::mapRow);
    }

    @Override
    public Optional<EmployeeDto> findById(Integer id) {
        return CrudUtil.executeQueryForOptional(
                "SELECT * FROM employee WHERE employee_id = ?",
                this::mapRow,
                id
        );
    }

    @Override
    public Integer save(EmployeeDto entity) {
        return CrudUtil.executeUpdateWithGeneratedKeys(
                "INSERT INTO employee (name, phone, email, address, hire_date, active) VALUES (?,?,?,?,?,?)",
                entity.getName(),
                entity.getPhone(),
                entity.getEmail(),
                entity.getAddress(),
                Date.valueOf(entity.getHireDate()),
                entity.isActive() ? 1 : 0
        );
    }

    @Override
    public void update(EmployeeDto entity) {
        CrudUtil.executeUpdate(
                "UPDATE employee SET name=?, phone=?, email=?, address=?, hire_date=?, active=? WHERE employee_id=?",
                entity.getName(),
                entity.getPhone(),
                entity.getEmail(),
                entity.getAddress(),
                Date.valueOf(entity.getHireDate()),
                entity.isActive() ? 1 : 0,
                entity.getEmployeeId()
        );
    }

    @Override
    public void deleteById(Integer id) {
        CrudUtil.executeUpdate("DELETE FROM employee WHERE employee_id = ?", id);
    }

    @Override
    public List<EmployeeDto> searchByName(String name) {
        return CrudUtil.executeQueryForList(
                "SELECT * FROM employee WHERE name LIKE ? ORDER BY employee_id",
                this::mapRow,
                "%" + name.trim() + "%"
        );
    }

    private EmployeeDto mapRow(ResultSet rs) throws SQLException {
        Date hireDate = rs.getDate("hire_date");
        return new EmployeeDto(
                rs.getInt("employee_id"),
                rs.getString("name"),
                rs.getString("phone"),
                rs.getString("email"),
                rs.getString("address"),
                hireDate != null ? hireDate.toLocalDate() : LocalDate.now(),
                rs.getBoolean("active")
        );
    }
}
