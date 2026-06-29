package edu.icet.repository;

import edu.icet.model.dto.SupplierDto;

import java.util.List;
import java.util.Optional;

public interface SupplierRepository extends CrudRepository<SupplierDto, Integer> {
    List<SupplierDto> searchByName(String name);
    Optional<SupplierDto> findById(Integer id);
}
