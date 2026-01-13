package edu.icet.service;

import edu.icet.model.dto.ProductDto;
import javafx.collections.ObservableList;

public interface ProductService {
    int addProduct(ProductDto productDto);
    void editProduct(ProductDto productDto);
    void deleteProduct(Integer id);
    ObservableList<ProductDto> getAllProducts();
    ObservableList<ProductDto> searchProducts(String name, Integer categoryId, Double minPrice, Double maxPrice);
}
