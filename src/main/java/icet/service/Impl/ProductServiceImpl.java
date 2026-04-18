package edu.icet.service.Impl;

import edu.icet.model.dto.ProductDto;
import edu.icet.repository.ProductRepository;
import edu.icet.service.ProductService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ProductServiceImpl implements ProductService {

    private final ProductRepository repository;

    public ProductServiceImpl(ProductRepository repository) {
        this.repository = repository;
    }

    @Override
    public int addProduct(ProductDto productDto) {
        return repository.addProduct(productDto);
    }

    @Override
    public void editProduct(ProductDto productDto) {
        repository.editProduct(productDto);
    }

    @Override
    public void deleteProduct(Integer id) {
        repository.deleteProduct(id);
    }

    @Override
    public ObservableList<ProductDto> getAllProducts() {
        return FXCollections.observableArrayList(repository.getProducts());
    }

    @Override
    public ObservableList<ProductDto> searchProducts(String name, Integer categoryId, Double minPrice, Double maxPrice) {
        return FXCollections.observableArrayList(repository.searchProducts(name, categoryId, minPrice, maxPrice));
    }
}
