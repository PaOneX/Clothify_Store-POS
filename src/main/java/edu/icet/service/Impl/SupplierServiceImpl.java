package edu.icet.service.Impl;

import edu.icet.model.dto.SupplierDto;
import edu.icet.repository.Iml.SupplierRepositoryImpl;
import edu.icet.repository.SupplierRepository;
import edu.icet.service.SupplierService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.ResultSet;

public class SupplierServiceImpl implements SupplierService {
    SupplierRepository repository = new SupplierRepositoryImpl();

    @Override
    public void addSupplier(SupplierDto supplierDTO) throws Exception {
        repository.addSupplier(supplierDTO);
    }

    @Override
    public void updateSupplier(SupplierDto supplierDTO) throws Exception {

    }

    @Override
    public void deleteSupplier(String id) throws Exception {

    }

    @Override
    public ObservableList<SupplierDto> getAllSuppliers() {
        ObservableList<SupplierDto> supplierDtos = FXCollections.observableArrayList();
        try {
            ResultSet rs = repository.getAllSuppliers();
            while (rs.next()) {
                supplierDtos.add(new SupplierDto(
                        rs.getString("supplier_id"),
                        rs.getString("supplier_name"),
                        rs.getString("phone"),
                        rs.getString("email"),
                        rs.getString("address")
                ));

            }
            return supplierDtos;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

        @Override
        public SupplierDto searchSupplier (String id) throws Exception {
            return null;
        }
    }
