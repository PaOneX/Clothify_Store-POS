package edu.icet.service.Impl;

import edu.icet.model.dto.ProductDto;
import edu.icet.repository.Iml.ProductRepositoryImpl;
import edu.icet.repository.ProductRepository;
import edu.icet.service.ProductService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class ProductServiceImpl implements ProductService {
    ProductRepository repository =new ProductRepositoryImpl();
    @Override
    public void addProduct(ProductDto productDto) throws Exception {
        repository.addProduct(productDto);
    }

    @Override
    public void editProduct(ProductDto productDto) {

    }

    @Override
    public void deleteProduct(Integer id) throws Exception {
        repository.deleteProduct(id);
    }

    @Override
    public ObservableList<ProductDto> getAllProducts() {
        ObservableList<ProductDto> products = FXCollections.observableArrayList();
        try {
            ResultSet resultSet = repository.getProducts();
            while (resultSet.next()) {
                products.add(new ProductDto(
                        resultSet.getInt("product_id"),
                        resultSet.getString("product_name"),
                        resultSet.getDouble("price"),
                        resultSet.getInt("quantity"),
                        resultSet.getInt("category_id"),
                        resultSet.getString("supplier_id")
                ));
            }
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return products;
    }
}
