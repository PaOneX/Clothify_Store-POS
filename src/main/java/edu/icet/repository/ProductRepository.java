package edu.icet.repository;

import edu.icet.model.dto.ProductDto;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public interface ProductRepository {
    void   addProduct(ProductDto productDto) throws Exception;
    void   editProduct(ProductDto productDto);
    void   deleteProduct(Integer id);
   ResultSet getProducts() throws SQLException;
}
