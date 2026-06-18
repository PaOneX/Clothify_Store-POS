package edu.icet.repository;

import edu.icet.model.dto.CustomerDto;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository {
    int save(CustomerDto customer);
    void update(CustomerDto customer);
    void delete(Integer customerId);
    List<CustomerDto> findAll();
    List<CustomerDto> search(String term);
    Optional<CustomerDto> findById(Integer customerId);
}
