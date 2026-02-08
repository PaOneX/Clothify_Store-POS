package edu.icet.service;

import edu.icet.model.dto.ProductDto;
import javafx.collections.ObservableList;

import java.util.List;

public interface ProductService {
    void addProduct(ProductDto productDto) throws Exception;
    void editProduct(ProductDto productDto);
    void deleteProduct(Integer id);
    ObservableList<ProductDto> getAllProducts();
}
