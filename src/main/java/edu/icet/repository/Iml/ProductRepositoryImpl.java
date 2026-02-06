package edu.icet.repository.Iml;

import edu.icet.db.DBConnection;
import edu.icet.model.dto.ProductDto;
import edu.icet.repository.ProductRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class ProductRepositoryImpl implements ProductRepository {
    @Override
    public void addProduct(ProductDto productDto) {
        Connection connection = null;
        try {
            connection = DBConnection.getInstance().getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO Products " +
                    "(product_name, description, price, qty_on_hand, category_id," +
                    " supplier_id) VALUES (?,?,?,?,?,?)");

            preparedStatement.setObject(1, productDto.getProductName());
            preparedStatement.setObject(2, productDto.getDescription());
            preparedStatement.setObject(3, productDto.getPrice());
            preparedStatement.setObject(4, productDto.getQty());
            preparedStatement.setObject(5, productDto.getCategory());
            preparedStatement.setObject(6, productDto.getSupplierId());

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void editProduct(ProductDto productDto) {

    }

    @Override
    public void deleteProduct(Integer id) {

    }

    @Override
    public List<ProductDto> getProducts() {
        return List.of();
    }
}
