package edu.icet.service.Impl;

import edu.icet.model.dto.SupplierDto;
import edu.icet.repository.SupplierRepository;
import edu.icet.service.SupplierService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository repository;

    public SupplierServiceImpl(SupplierRepository repository) {
        this.repository = repository;
    }

    @Override
    public int addSupplier(SupplierDto supplierDto) {
        return repository.save(supplierDto);
    }

    @Override
    public void updateSupplier(SupplierDto supplierDto) {
        repository.update(supplierDto);
    }

    @Override
    public void deleteSupplier(Integer id) {
        repository.deleteById(id);
    }

    @Override
    public ObservableList<SupplierDto> getAllSuppliers() {
        return FXCollections.observableArrayList(repository.findAll());
    }

    @Override
    public ObservableList<SupplierDto> searchSuppliers(String name) {
        if (name == null || name.isBlank()) {
            return getAllSuppliers();
        }
        return FXCollections.observableArrayList(repository.searchByName(name));
    }
}
