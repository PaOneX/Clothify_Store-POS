package edu.icet.repository.custom;

import edu.icet.model.dto.SupplierDto;

import java.sql.ResultSet;

public interface SupplierRepository {
    void addSupplier(SupplierDto supplierDto) throws Exception;
    void updateSupplier(SupplierDto supplierDto);
    void deleteSupplier(Integer id);
    ResultSet getAllSuppliers() throws Exception;
    void searchSupplier(Integer id);
}
