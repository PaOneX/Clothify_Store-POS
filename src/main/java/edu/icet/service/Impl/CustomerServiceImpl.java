package edu.icet.service.Impl;

import edu.icet.model.dto.CustomerDto;
import edu.icet.model.dto.OrderDto;
import edu.icet.repository.CustomerRepository;
import edu.icet.repository.OrderRepository;
import edu.icet.service.CustomerService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;

public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;

    public CustomerServiceImpl(CustomerRepository customerRepository, OrderRepository orderRepository) {
        this.customerRepository = customerRepository;
        this.orderRepository = orderRepository;
    }

    @Override
    public int save(CustomerDto customer) {
        return customerRepository.save(customer);
    }

    @Override
    public void update(CustomerDto customer) {
        customerRepository.update(customer);
    }

    @Override
    public void delete(Integer customerId) {
        customerRepository.delete(customerId);
    }

    @Override
    public ObservableList<CustomerDto> getAll() {
        return FXCollections.observableArrayList(customerRepository.findAll());
    }

    @Override
    public ObservableList<CustomerDto> search(String term) {
        if (term == null || term.isBlank()) {
            return getAll();
        }
        return FXCollections.observableArrayList(customerRepository.search(term));
    }

    @Override
    public CustomerDto findById(Integer customerId) {
        return customerRepository.findById(customerId).orElse(null);
    }

    @Override
    public List<OrderDto> getPurchaseHistory(Integer customerId) {
        List<OrderDto> orders = orderRepository.findByCustomerId(customerId);
        for (OrderDto order : orders) {
            order.setItems(orderRepository.findItemsByOrderId(order.getOrderId()));
        }
        return orders;
    }
}
