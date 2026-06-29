package edu.icet.repository;

import edu.icet.model.dto.ProductDto;

import java.util.List;

public interface ProductRepository {
    int addProduct(ProductDto productDto);
    void editProduct(ProductDto productDto);
    void deleteProduct(Integer id);
    List<ProductDto> getProducts();
    List<ProductDto> searchProducts(String name, Integer categoryId, Double minPrice, Double maxPrice);
}
