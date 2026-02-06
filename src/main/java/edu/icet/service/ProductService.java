package edu.icet.service;

import edu.icet.model.dto.ProductDto;

import java.util.List;

public interface ProductService {
    void addProduct(ProductDto productDto);
    void editProduct(ProductDto productDto);
    void deleteProduct(Integer id);
    List<ProductDto> getProducts();
}
