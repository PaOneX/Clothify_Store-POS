package edu.icet.repository.Impl;

import edu.icet.model.dto.CustomerDto;
import edu.icet.repository.CustomerRepository;
import edu.icet.util.CrudUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class CustomerRepositoryImpl implements CustomerRepository {

    @Override
    public int save(CustomerDto customer) {
        return CrudUtil.executeUpdateWithGeneratedKeys(
                "INSERT INTO customer (name, phone, email, address, active) VALUES (?,?,?,?,?)",
                customer.getName(),
                customer.getPhone(),
                customer.getEmail(),
                customer.getAddress(),
                customer.getActive() != null && customer.getActive() ? 1 : 1
        );
    }

    @Override
    public void update(CustomerDto customer) {
        CrudUtil.executeUpdate(
                "UPDATE customer SET name=?, phone=?, email=?, address=?, active=? WHERE customer_id=?",
                customer.getName(),
                customer.getPhone(),
                customer.getEmail(),
                customer.getAddress(),
                customer.getActive() != null && customer.getActive() ? 1 : 0,
                customer.getCustomerId()
        );
    }

    @Override
    public void delete(Integer customerId) {
        CrudUtil.executeUpdate("DELETE FROM customer WHERE customer_id = ?", customerId);
    }

    @Override
    public List<CustomerDto> findAll() {
        return CrudUtil.executeQueryForList(
                "SELECT customer_id, name, phone, email, address, active FROM customer ORDER BY name",
                this::mapRow
        );
    }

    @Override
    public List<CustomerDto> search(String term) {
        String like = "%" + term.trim() + "%";
        return CrudUtil.executeQueryForList(
                "SELECT customer_id, name, phone, email, address, active FROM customer WHERE name LIKE ? OR phone LIKE ? ORDER BY name",
                this::mapRow,
                like, like
        );
    }

    @Override
    public Optional<CustomerDto> findById(Integer customerId) {
        return CrudUtil.executeQueryForOptional(
                "SELECT customer_id, name, phone, email, address, active FROM customer WHERE customer_id = ?",
                this::mapRow,
                customerId
        );
    }

    @Override
    public Optional<CustomerDto> findByPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return Optional.empty();
        }
        return CrudUtil.executeQueryForOptional(
                "SELECT customer_id, name, phone, email, address, active FROM customer WHERE phone = ?",
                this::mapRow,
                phone.trim()
        );
    }

    private CustomerDto mapRow(ResultSet rs) throws SQLException {
        return new CustomerDto(
                rs.getInt("customer_id"),
                rs.getString("name"),
                rs.getString("phone"),
                rs.getString("email"),
                rs.getString("address"),
                rs.getInt("active") == 1
        );
    }
}
