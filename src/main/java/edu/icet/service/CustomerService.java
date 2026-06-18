package edu.icet.service;

import edu.icet.model.dto.CustomerDto;
import edu.icet.model.dto.OrderDto;
import javafx.collections.ObservableList;

import java.util.List;

public interface CustomerService {
    int save(CustomerDto customer);
    void update(CustomerDto customer);
    void delete(Integer customerId);
    ObservableList<CustomerDto> getAll();
    ObservableList<CustomerDto> search(String term);
    CustomerDto findById(Integer customerId);
    List<OrderDto> getPurchaseHistory(Integer customerId);
}
