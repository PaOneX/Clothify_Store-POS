package edu.icet.repository.Impl;

import edu.icet.model.dto.SupplierDto;
import edu.icet.repository.SupplierRepository;
import edu.icet.util.CrudUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class SupplierRepositoryImpl implements SupplierRepository {

    @Override
    public List<SupplierDto> findAll() {
        return CrudUtil.executeQueryForList("SELECT * FROM supplier ORDER BY supplier_id", this::mapRow);
    }

    @Override
    public Optional<SupplierDto> findById(Integer id) {
        return CrudUtil.executeQueryForOptional(
                "SELECT * FROM supplier WHERE supplier_id = ?",
                this::mapRow,
                id
        );
    }

    @Override
    public Integer save(SupplierDto entity) {
        return CrudUtil.executeUpdateWithGeneratedKeys(
                "INSERT INTO supplier (supplier_name, phone, email, address) VALUES (?,?,?,?)",
                entity.getName(),
                entity.getContact(),
                entity.getEmail(),
                entity.getAddress()
        );
    }

    @Override
    public void update(SupplierDto entity) {
        CrudUtil.executeUpdate(
                "UPDATE supplier SET supplier_name=?, phone=?, email=?, address=? WHERE supplier_id=?",
                entity.getName(),
                entity.getContact(),
                entity.getEmail(),
                entity.getAddress(),
                entity.getId()
        );
    }

    @Override
    public void deleteById(Integer id) {
        CrudUtil.executeUpdate("DELETE FROM supplier WHERE supplier_id = ?", id);
    }

    @Override
    public List<SupplierDto> searchByName(String name) {
        return CrudUtil.executeQueryForList(
                "SELECT * FROM supplier WHERE supplier_name LIKE ? ORDER BY supplier_id",
                this::mapRow,
                "%" + name.trim() + "%"
        );
    }

    private SupplierDto mapRow(ResultSet rs) throws SQLException {
        return new SupplierDto(
                rs.getInt("supplier_id"),
                rs.getString("supplier_name"),
                rs.getString("phone"),
                rs.getString("email"),
                rs.getString("address")
        );
    }
}
