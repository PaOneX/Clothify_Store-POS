package edu.icet.service;

import edu.icet.model.dto.SupplierDto;
import javafx.collections.ObservableList;

import java.sql.ResultSet;

public interface SupplierService {
    void addSupplier(SupplierDto supplierDTO) throws Exception;
    void updateSupplier(SupplierDto supplierDTO) throws Exception;
    void deleteSupplier(String id) throws Exception;
    ObservableList<SupplierDto> getAllSuppliers() throws Exception;
    SupplierDto searchSupplier(String id) throws Exception;
}
