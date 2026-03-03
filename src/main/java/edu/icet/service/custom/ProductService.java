package edu.icet.service.custom;

import edu.icet.model.dto.ProductDto;
import javafx.collections.ObservableList;

public interface ProductService {
    void addProduct(ProductDto productDto) throws Exception;
    void editProduct(ProductDto productDto);
    void deleteProduct(Integer id) throws Exception;
    ObservableList<ProductDto> getAllProducts();
}
