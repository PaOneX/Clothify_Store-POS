package edu.icet.repository.custom;

import edu.icet.model.dto.ProductDto;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface ProductRepository {
    void   addProduct(ProductDto productDto) throws Exception;
    void   editProduct(ProductDto productDto);
    void   deleteProduct(Integer id) throws Exception;
   ResultSet getProducts() throws SQLException;
}
