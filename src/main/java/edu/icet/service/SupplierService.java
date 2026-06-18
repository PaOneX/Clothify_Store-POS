package edu.icet.service;

import edu.icet.model.dto.SupplierDto;
import javafx.collections.ObservableList;

public interface SupplierService {
    int addSupplier(SupplierDto supplierDto);
    void updateSupplier(SupplierDto supplierDto);
    void deleteSupplier(Integer id);
    ObservableList<SupplierDto> getAllSuppliers();
    ObservableList<SupplierDto> searchSuppliers(String name);
}
